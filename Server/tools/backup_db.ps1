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

    # dump tool: MySQL = mysqldump.exe; MariaDB novo = mariadb-dump.exe. Procura os dois.
    $dumpNames = @("mysqldump.exe", "mariadb-dump.exe")
    $dump = $null
    $candidates = New-Object System.Collections.Generic.List[string]

    # 1. MySqlBinLocation do Database.ini (os dois nomes)
    if (-not [string]::IsNullOrWhiteSpace($binDir)) {
        foreach ($n in $dumpNames) { $candidates.Add((Join-Path $binDir $n)) }
    }
    # 2. pasta do mysqld que esta RODANDO (fonte mais confiavel - serve p/ MariaDB do XAMPP)
    try {
        $mysqld = Get-Process mysqld -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($mysqld -and $mysqld.Path) {
            $bin = Split-Path $mysqld.Path
            foreach ($n in $dumpNames) { $candidates.Add((Join-Path $bin $n)) }
        }
    } catch {}
    # 3. PATH
    foreach ($n in $dumpNames) {
        $cmd = Get-Command $n -ErrorAction SilentlyContinue
        if ($cmd) { $candidates.Add($cmd.Source) }
    }
    # 4. locais comuns (XAMPP, MariaDB, MySQL)
    foreach ($base in @("C:\xampp\mysql\bin", "C:\Program Files\MariaDB", "C:\Program Files\MySQL", "C:\Program Files (x86)\MySQL", "C:\MariaDB", "C:\MySQL", "C:\wamp64\bin")) {
        if (Test-Path $base) {
            foreach ($n in $dumpNames) {
                Get-ChildItem $base -Recurse -Filter $n -ErrorAction SilentlyContinue | ForEach-Object { $candidates.Add($_.FullName) }
            }
        }
    }
    foreach ($c in $candidates) { if ($c -and (Test-Path $c)) { $dump = $c; break } }
    if (-not $dump) {
        Log-Err ("dump tool nao encontrado (mysqldump/mariadb-dump). Procurado em: " + ($candidates -join " | "))
        exit 1
    }
    Write-Host ("dump tool: {0}" -f $dump)

    $tmp   = Join-Path $BackupDir ("db_{0}.sql.part" -f $stamp)
    $final = Join-Path $BackupDir ("db_{0}.sql"      -f $stamp)

    # --- roda o dump (senha via --password= pra nao precisar de espaco) ---
    $dumpArgs = @("--user=$login")
    if (-not [string]::IsNullOrEmpty($pass)) { $dumpArgs += "--password=$pass" }
    $dumpArgs += @("--single-transaction", "--routines", "--events", "--default-character-set=utf8mb4", $dbName, "--result-file=$tmp")

    & $dump @dumpArgs 2> (Join-Path $BackupDir "mysqldump_stderr.tmp")
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
