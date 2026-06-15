# ============================================================
# Backup do MySQL do L2 Ikarus - robusto e auto-curavel
# Le as credenciais do PROPRIO Database.ini do game (fonte unica:
# se a senha do MySQL mudar, basta atualizar o Database.ini que o
# backup acompanha sozinho). NUNCA gera arquivo 0 KB silencioso:
# se o dump falhar/vier vazio, descarta e registra no log de erro.
#
# Uso na VPS (Agendador de Tarefas, de hora em hora):
#   powershell -ExecutionPolicy Bypass -File C:\Server\game\tools\backup_db.ps1
#
# Parametros (ajuste os defaults pro caminho real da VPS se preciso):
param(
    [string]$IniPath   = "C:\Server\game\config\Database.ini",
    [string]$BackupDir = "C:\Server\backup",
    [int]$KeepDays     = 30,
    [int]$MinSizeKB    = 5   # abaixo disso = dump falhou
)

$ErrorActionPreference = "Stop"
$stamp   = Get-Date -Format "yyyy-MM-dd_HH-mm"
$logFile = Join-Path $BackupDir "backup_errors.log"

function Log-Err($msg) {
    $line = "[{0}] {1}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $msg
    if (-not (Test-Path $BackupDir)) { New-Item -ItemType Directory -Force $BackupDir | Out-Null }
    Add-Content -Path $logFile -Value $line -Encoding utf8
    Write-Host $line
}

try {
    if (-not (Test-Path $IniPath))   { Log-Err "Database.ini nao encontrado em $IniPath"; exit 1 }
    if (-not (Test-Path $BackupDir)) { New-Item -ItemType Directory -Force $BackupDir | Out-Null }

    # --- le Database.ini ---
    $ini = Get-Content $IniPath
    function Get-Ini($key) {
        $l = $ini | Where-Object { $_ -match "^\s*$key\s*=" } | Select-Object -First 1
        if ($null -eq $l) { return "" }
        return ($l -replace "^\s*$key\s*=\s*", "").Trim()
    }
    $login   = Get-Ini "Login"
    $pass    = Get-Ini "Password"
    $url     = Get-Ini "URL"
    $binDir  = (Get-Ini "MySqlBinLocation").Replace("/", "\")

    # nome do banco a partir da URL jdbc:mysql://host/DBNAME?params
    $dbName = ""
    if ($url -match "//[^/]+/([^?]+)") { $dbName = $Matches[1] }
    if ([string]::IsNullOrWhiteSpace($dbName)) { Log-Err "Nao consegui extrair o nome do banco da URL"; exit 1 }

    # mysqldump: usa o MySqlBinLocation do ini; se nao existir, tenta o do PATH
    $dump = Join-Path $binDir "mysqldump.exe"
    if (-not (Test-Path $dump)) {
        $cmd = Get-Command mysqldump.exe -ErrorAction SilentlyContinue
        if ($cmd) { $dump = $cmd.Source } else { Log-Err "mysqldump.exe nao encontrado ($dump nem no PATH)"; exit 1 }
    }

    $tmp   = Join-Path $BackupDir ("db_{0}.sql.part" -f $stamp)
    $final = Join-Path $BackupDir ("db_{0}.sql"      -f $stamp)

    # --- roda o dump (senha via --password= pra nao precisar de espaco) ---
    $args = @("--user=$login")
    if (-not [string]::IsNullOrEmpty($pass)) { $args += "--password=$pass" }
    $args += @("--single-transaction", "--routines", "--events", "--default-character-set=utf8mb4", $dbName, "--result-file=$tmp")

    & $dump @args 2> (Join-Path $BackupDir "mysqldump_stderr.tmp")
    $code = $LASTEXITCODE

    # --- valida ---
    $sizeKB = 0
    if (Test-Path $tmp) { $sizeKB = [math]::Round((Get-Item $tmp).Length / 1KB, 1) }

    if ($code -ne 0 -or $sizeKB -lt $MinSizeKB) {
        $stderr = ""
        $errTmp = Join-Path $BackupDir "mysqldump_stderr.tmp"
        if (Test-Path $errTmp) { $stderr = (Get-Content $errTmp -Raw).Trim() }
        if (Test-Path $tmp) { Remove-Item $tmp -Force }
        Log-Err ("DUMP FALHOU (exit={0}, tamanho={1}KB). Provavel senha errada no Database.ini. stderr: {2}" -f $code, $sizeKB, $stderr)
        exit 1
    }

    Move-Item $tmp $final -Force
    Remove-Item (Join-Path $BackupDir "mysqldump_stderr.tmp") -Force -ErrorAction SilentlyContinue
    Write-Host ("OK: {0} ({1} KB)" -f $final, $sizeKB)

    # --- rotacao: apaga backups com mais de KeepDays dias ---
    if ($KeepDays -gt 0) {
        Get-ChildItem $BackupDir -Filter "db_*.sql" |
            Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-$KeepDays) } |
            Remove-Item -Force -ErrorAction SilentlyContinue
    }
}
catch {
    Log-Err ("ERRO inesperado: {0}" -f $_.Exception.Message)
    exit 1
}
