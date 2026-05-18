# L2 Jikarus — Game Server

Source customizado do servidor Lineage 2 privado **Samurai Crow**, baseado no L2JDev com modificações exclusivas da TECX SOFTHOUSE.

## Customizações

- Renomeação do projeto de `l2jmobius` para `l2jikarus`
- Sistema de Voice Chat por proximidade integrado (`VoiceChatManager`)
- Ajustes de rates, configs e balanceamento personalizados
- Modificações de gameplay exclusivas do servidor

## Estrutura

```
Server/
├── java/          # Source Java do GameServer e LoginServer
├── game/
│   ├── config/    # Configurações do servidor
│   └── data/      # XMLs de dados (mobs, skills, items, etc.)
├── login/         # Login Server
├── libs/          # Dependências (HikariCP, MySQL Connector)
└── build.xml      # Build com Apache Ant
```

## Build

Requer Java 17+ e Apache Ant:

```bash
ant -f build.xml
```

## Configuração

```bash
cp game/config/Database.ini.example game/config/Database.ini
# Editar Database.ini com suas credenciais
```

## Desenvolvido por

**TECX SOFTHOUSE** — [github.com/MANINtecn](https://github.com/MANINtecn)