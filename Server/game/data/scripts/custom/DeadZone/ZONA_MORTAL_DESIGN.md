# 🔴 MOD: ZONA MORTAL (Dead Zone)

> Área de mundo aberto de risco extremo. Morreu lá dentro, perde TUDO.
> Documento de design — L2 Ikarus Intercrow.

---

## 1. Conceito

Uma área do mapa **aberto** (não é instância) escolhida para ser a zona mais perigosa
e mais valorizada do servidor. Drops e recompensas altíssimos lá dentro, mas o preço
de morrer é perder absolutamente tudo. Roda **24 horas por dia**, sem horário.

### Sistema multi-zona por grade (progressão endgame)

Não é uma zona única — é um **sistema escalonado**, uma zona por grade de equipamento,
cada uma centrada numa cidade temática com recompensas à altura da grade. Amarra com a
progressão controlada de grades do servidor (D→C→B→A liberados aos poucos) e com o
level cap de quests (lvl 20 = fim do grade-D = primeira zona).

| Zona | Grade | Nível | Cidade | Status |
|---|---|---|---|---|
| 1ª | D | ~20–40 | **Dion** | a implementar primeiro |
| 2ª | C | ~40–52 | a definir | futura |
| 3ª | B | ~52–61 | a definir | futura |
| 4ª | A | ~61+ | a definir | quando liberar A-grade |

Cada zona é uma **instância configurável** do mesmo mod: loc própria, grade-teto,
faixa de nível e recompensas. Quando uma grade nova é liberada no servidor, abre-se
uma nova zona seguindo a mesma lógica — zero retrabalho de código.

---

## 2. Regras definidas (game design)

| Regra | Decisão |
|---|---|
| **O que perde ao morrer** | TUDO: equipamento vestido + inventário + adena |
| **Corpo (cadáver saqueável)** | Ao morrer, deixa um **corpo no chão** com a aparência do morto, carregando todo o loot |
| **Saque** | Jogador clica no corpo → abre janela → cada item sai com **delay de 2s** (configurável) |
| **Interrupção do saque** | Se o saqueador **apanhar ou se mover** durante os 2s, o item NÃO sai (tem que clicar de novo) |
| **Disputa** | **Livre** — vários jogadores podem saquear o mesmo corpo ao mesmo tempo, corrida pelo loot |
| **Gatilho de morte** | Qualquer morte conta (PvE, PvP, queda) |
| **Aviso ao entrar** | 1ª vez: janela HTML (grava que viu). Depois: mensagem grande vermelha na tela |
| **Auto-farm** | Bloqueado dentro da zona |
| **Duração do corpo** | 120s (configurável) |
| **Alertas globais** | Announcements 24h via sistema existente |

---

## 2b. Controle de acesso (anti-burla, sistema 3-strikes)

Cada zona tem uma **grade-teto** e uma **faixa de nível**. Só entra quem se encaixa,
e o script vigia continuamente pra impedir burla.

### Validação de entrada (`onEnter`)
- **Grade equipada**: todos os itens equipados que têm grade devem ser ≤ grade-teto da zona
  (ex: zona D não aceita ninguém com peça C/B/A equipada)
- **Nível**: dentro da faixa fechada (mín e máx). Barra quem está **abaixo** (lvl 15 não
  entra na zona D) **e** quem está **acima** (lvl 50 não entra na zona D)
- Falhou qualquer regra → bloqueado/teleportado pra fora na hora

### Verificação contínua (task a cada 60s)
Varre todos os jogadores dentro de cada zona e revalida a grade equipada. Pega a burla
clássica ("tira equip forte → entra → reequipa dentro"):

```
1ª violação → KICK + mensagem na tela (vermelha) explicando o motivo e o aviso 1/3
2ª violação → KICK + mensagem na tela explicando 2/3 e avisando que a próxima = JAIL
3ª violação → JAIL 5 min + mensagem explicando que foi preso por burlar a regra
               Após cumprir, contador volta a ZERO
```

**Toda penalidade é transparente** — o jogador SEMPRE recebe mensagem clara do motivo
(equipamento acima da grade permitida) e da consequência (qual strike, o que vem a seguir).
Nada de kick/jail silencioso.

Textos configuráveis (com placeholders):
- `DeadZoneKickWarn1` — ex: "Seu equipamento excede a grade desta zona! Você foi removido. Violação 1/3. Na 3ª você será preso."
- `DeadZoneKickWarn2` — ex: "Equipamento acima da grade! Removido novamente. Violação 2/3 — ÚLTIMA CHANCE antes da prisão."
- `DeadZoneJailMsg` — ex: "Você burlou a regra da Zona Mortal pela 3ª vez e foi preso por 5 minutos."

- **Slots validados**: tudo que tem grade (armas, armaduras, joias)
- **Tempo de jail**: 5 min (`DeadZoneJailMinutes`)
- **Reset do contador**: após cumprir o jail (cada ciclo de 3 strikes é independente)

---

## 3. Fluxo completo

### Ao ENTRAR na zona (`onEnter`)
1. Para o auto-play imediatamente (`AutoPlayTaskManager.stopAutoPlay`)
2. Marca flag interna "dentro da zona mortal" (bloqueia reativar autoplay)
3. **Primeira vez na vida**: abre janela HTML de aviso → grava `DEADZONE_WARNED` em PlayerVariables
4. **Próximas vezes**: mensagem vermelha grande na tela (`ExShowScreenMessage`)

### DENTRO da zona
- Auto-play permanece bloqueado (qualquer tentativa de ligar é barrada)
- Jogador joga manualmente (parte da proposta hardcore)

### Ao MORRER dentro (`OnCreatureDeath` listener)
1. Verifica se a morte foi dentro da zona
2. **Mecânica de QUEBRA** (itens equipados — set + acessórios):
   - Sorteia uma % aleatória entre `BreakMinPercent` e `BreakMaxPercent` (ex: 10–30%) a cada morte
   - Aplica sobre a qtd de equipados → quantos quebram (ex: 10 itens, 20% = 2 quebram)
   - Sorteia ALEATORIAMENTE quais (Collections.shuffle)
   - Os quebrados viram **"Material: Leather Scraps"** (item 92911, existente, Sealed) — sucata stackable
     que dá **Craft Points +3000** (skill 40078) pro Random Craft ao ser usada. Micro-uso real e temático.
   - Os intactos vão inteiros pro corpo
   - COMPLEMENTA o perde-tudo: a % quebra (vira lixo) + o resto vai pro corpo saqueável
3. Coleta **todo** o loot: equipados (com quebra aplicada) + inventário + adena
   - Exceção: lista de itens protegidos configurável (quest items, moedas de evento)
   - **Itens Sealed**: o mod IGNORA a restrição de comércio e força a transferência.
     Como o drop é manual via código (não trade/mail/venda), a flag "Cannot be sold/traded"
     não se aplica. NÃO precisa remover o sealed das quests — retail preservado fora da zona.
     Ver `Item.isDropable()` (linha ~544): o servidor já dropa sealed quando PK morre.
4. Remove os itens do jogador
4. Spawna o **corpo** (NPC com a aparência do morto, parado/deitado) na posição exata
5. O corpo **guarda o loot coletado** num container temporário
6. Corpo fica disponível por 120s (config)

### Saquear o corpo (interação)
1. Qualquer jogador clica no corpo → abre janela HTML com a **lista de itens**
2. Clica em um item → inicia saque com **delay de 2s** (config) — animação/cast visível
3. Durante os 2s, se o saqueador **levar dano ou se mover** → saque **cancelado**, item permanece no corpo
4. Completou os 2s → item transferido pro inventário do saqueador
   - **Itens Sealed permanecem Sealed** ao serem saqueados: o novo dono pode usar/equipar,
     mas não pode revender. Mantém o espírito retail e não infla a economia
5. **Disputa livre**: vários jogadores saqueiam ao mesmo tempo; quem completar o delay primeiro leva o item
6. Quando o corpo **expira** (120s):
   - Itens restantes → caem no chão **ou** são destruídos (configurável `DeadZoneLootOnExpire`)

### Ao SAIR da zona (`onExit`)
1. Remove a flag interna
2. Libera o auto-play novamente

### Alertas em dois níveis (por público)

**A) Global — atração (announce NORMAL, servidor inteiro)**
- A cada `DeadZoneAnnounceInterval` minutos (default 30), announcement no chat de
  **todos os jogadores online**
- Objetivo: isca/convite, atrair gente pra zona. Discreto, não intrusivo
- Texto: `DeadZoneAnnounceText`

**B) Interno — perigo (announce CRITICAL, só quem está dentro)**
- A cada `DeadZoneCriticalInterval` segundos (default 90), critical announce **apenas
  para os jogadores dentro da zona** (lista controlada pelo onEnter/onExit)
- Objetivo: reforçar o perigo constante, manter a tensão de "qualquer erro perde tudo"
- Texto: `DeadZoneCriticalText`
- Mais frequente que o global, mas não a ponto de virar spam

---

## 4. Arquitetura técnica

**100% via scripts** (`data/scripts/custom/DeadZone/`) — NÃO mexe no JAR do core.
Tudo sincroniza pelo auto-sync do git.

| Componente | Arquivo | O que faz |
|---|---|---|
| Script principal | `DeadZone.java` | Listeners onEnter/onExit/onDeath, lógica do corpo e saque |
| Configuração | `config/Custom/DeadZone.ini` | Todos os parâmetros ajustáveis |
| Zona no mapa | `data/zones/DeadZone.xml` | Polígono (ZoneNPoly) na loc escolhida |
| Janela de aviso | `data/html/mods/deadzone/warning.htm` | HTML mostrado na 1ª entrada |
| Janela do corpo | `data/html/mods/deadzone/corpse.htm` | Lista de itens do corpo (gerada dinâmica) |
| Documentação | este arquivo | Design e referência |

### Peças do L2JMobius reutilizadas
- **`ScriptZone`** — tipo de zona com `onEnter`/`onExit` controlável por script
- **`Doppelganger`** (`model/actor/instance/Doppelganger.java`) — base visual do corpo (aparência do morto). Spawnado parado/morto, não-atacável, serve de "cadáver" clicável
- **`AutoPlayTaskManager.stopAutoPlay(player)`** — desliga o auto-farm
- **`ExShowScreenMessage`** — mensagem grande na tela
- **`OnCreatureDeath`** event listener — captura a morte
- **`PlayerVariables`** — grava se o jogador já viu o aviso
- **`ThreadPool.schedule`** — delay de 2s do saque por item
- Container temporário (mapa em memória no script) — guarda o loot de cada corpo

---

## 5. Configurações do mod (`DeadZone.ini`) — planejado

```ini
# ====== GLOBAL (vale pra todas as zonas) ======
# Liga/desliga o mod inteiro
DeadZoneEnabled = True

# --- Controle de acesso (anti-burla) ---
# Intervalo da verificacao continua de grade (segundos)
DeadZoneCheckInterval = 60
# Tempo de jail na 3a violacao (minutos)
DeadZoneJailMinutes = 5
# Numero de violacoes ate o jail
DeadZoneMaxViolations = 3
# Mensagens de penalidade (sempre explicam motivo + consequencia)
DeadZoneKickWarn1 = Seu equipamento excede a grade desta zona! Voce foi removido. Violacao 1/3. Na 3a voce sera preso.
DeadZoneKickWarn2 = Equipamento acima da grade! Removido novamente. Violacao 2/3 - ULTIMA CHANCE antes da prisao.
DeadZoneJailMsg = Voce burlou a regra da Zona Mortal pela 3a vez e foi preso por 5 minutos.

# Duracao do corpo no chao (segundos) antes de expirar
DeadZoneCorpseDuration = 120

# Delay de saque por item (segundos)
DeadZoneLootDelay = 2

# Saque cancela se o saqueador apanhar ou se mover durante o delay
DeadZoneLootInterruptible = True

# Quando o corpo expira com itens dentro: DROP (cai no chao) ou DESTROY (perde pra sempre)
DeadZoneLootOnExpire = DROP

# Dropar adena tambem
DeadZoneDropAdena = True

# Itens que NUNCA dropam (quest items, moedas de evento) - IDs separados por virgula
DeadZoneProtectedItems = 57000,91663

# Texto da mensagem na tela ao entrar
DeadZoneScreenMessage = ATENCAO! Voce entrou na ZONA MORTAL. Se morrer aqui, perde TUDO!

# --- Alerta GLOBAL (announce normal, servidor inteiro - atracao) ---
# Intervalo do announce global (minutos, 0 = desliga)
DeadZoneAnnounceInterval = 30
DeadZoneAnnounceText = A ZONA MORTAL esta ativa! Drops valiosos aguardam os corajosos - mas quem morrer la perde TUDO.

# --- Alerta INTERNO (critical announce, so quem esta dentro - perigo) ---
# Intervalo do critical interno (segundos, 0 = desliga)
DeadZoneCriticalInterval = 90
DeadZoneCriticalText = PERIGO! Voce esta na ZONA MORTAL. Um descuido e voce perde TUDO.

# ====== POR ZONA (cada zona tem seu bloco) ======
# Exemplo: Zona D em Dion
# Zone1.Name = Zona Mortal de Dion
# Zone1.ZoneId = <id da zona no DeadZone.xml>
# Zone1.MaxGrade = D            (grade-teto: D, C, B, A)
# Zone1.MinLevel = 20
# Zone1.MaxLevel = 40
# Zone1.City = Dion
# (recompensas/drops da zona definidos a parte conforme a grade)
```

---

## 6. Pendências / decisões em aberto

- [ ] **Localização da Zona D (Dion)** — usuário vai trazer as coordenadas (polígono) da 1ª zona
- [ ] **Cidades das zonas C e B** — a definir (futuro)
- [ ] **Recompensas/drops por grade** — definir o que cada zona dropa (D, depois C, B)

### Loot de mobs na zona (decidido 2026-05-31, implementar na fase de MOBS)
- **Servidor será ESSENCE** (auto-loot ON no servidor todo no futuro), MAS auto-loot OFF dentro das zonas.
- **Duração no chão**: mantido o global `AutoDestroyDroppedItemAfter = 600` (10 min). Sem mudança.
- **Solução = flag `ZoneId.DEAD_ZONE` (patch no core)**:
  1. Adicionar `DEAD_ZONE` no enum `ZoneId`.
  2. `DeadZone.java` marca o jogador com a flag no onEnterZone (`setInsideZone`) e remove no onExitZone.
  3. `Attackable.java` (linha ~1014, antes de `player.doAutoLoot`) checa `!player.isInsideZone(ZoneId.DEAD_ZONE)`.
  - Resolve de uma vez: auto-loot OFF na zona + adena no chão na zona + base p/ regras core futuras.
  - Recompila o JAR (deploy manual, como o CityDomination). É um patch único e definitivo.
- **DECIDIDO (2026-05-31): adiar este patch para a fase de mobs** (só importa quando a zona tiver mobs).
  Fases 1-2-3 do mod são 100% script e seguem sem ele.
- [ ] **Loot no expire do corpo**: DROP (cai no chão) ou DESTROY (perde pra sempre)? — default proposto: DROP
- [ ] **Lista de itens protegidos** definitiva (quais nunca dropam)
- [ ] **Texto final** da janela de aviso e da mensagem de tela
- [ ] **Intervalo e texto** dos announcements 24h
- [ ] **Aparência do corpo**: usar Doppelganger deitado, ou um NPC genérico tipo "Corpo de [nome]"? — a validar na implementação

---

## 7. Status

| Etapa | Status |
|---|---|
| Design e regras | ✅ Definido |
| Viabilidade técnica | ✅ Confirmada (tudo via script) |
| Loc da zona (Dion) | ✅ Mapeada (id 90001, 39 vértices) |
| **Fase 1** (zona/avisos/autoplay/alertas) | ✅ Implementada e TESTADA local |
| **Fase 2** (morte/corpo/saque) | ⏳ Em andamento |
| **Fase 3** (acesso grade/nível/jail + Gatekeeper) | ⏳ |
| Mobs e recompensas | ⏳ |
| Deploy VPS | ⏳ |

### Notas de implementação (Fase 1)
- Screen message (`ExShowScreenMessage`) é SEMPRE branco — cliente não colore. Para "vermelho"
  usamos `CreatureSay` com `ChatType.CRITICAL_ANNOUNCE` no chat, junto da mensagem central.
- Bloqueio de autoplay: task de 1s que chama `AutoPlayTaskManager.stopAutoPlay` + avisa
  (não há evento de autoplay pra interceptar; stopAutoPlay sozinho não impede religar).
- Config lido via `ConfigReader("./config/Custom/DeadZone.ini")` direto no script (sem mexer no core).
