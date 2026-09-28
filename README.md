# Patamar — Beta v0.1.1 (patch de funcionalidade)

Guia de eventos urbanos em Curitiba. App Android beta gerado conforme spec —
Kotlin + MVVM + Repository Pattern, sem backend real (Room local + mock data).

## Patch v0.1.1 — o que mudou

Patch de acabamento e funcionalidade em cima do beta v0.1 original, sem trocar
arquitetura. Três problemas visuais/funcionais corrigidos:

- **Perfil**: `fragment_profile.xml` reconstruído com cards (mesmo token de
  `bg_card_event`), ícones monocromáticos e chevron nas linhas clicáveis, em
  vez da pilha de `TextView` sem hierarquia visual que existia antes.
- **Explorar**: cards de evento (destaque e lista) agora carregam uma foto
  real via Coil em vez de um bloco de cor sólida. Ver seção "Segurança" sobre
  a exceção de rede que isso introduz.
- **Mapa**: tile source trocado de `TileSourceFactory.MAPNIK` (OSM padrão,
  colorido e cheio de rótulo) para CartoDB Dark Matter — minimalista e já
  combina com o tema escuro do app. Continua OSMDroid (Opção A), sem
  migração de SDK; atribuição atualizada para "© OpenStreetMap contributors
  © CARTO".
- **Explorar × Filtros**: os filtros salvos em `FilterActivity` (categorias)
  agora também restringem as seções do Explorar — antes só valiam pro Mapa.

## Como abrir

1. Abra a pasta `patamar/` no Android Studio (Hedgehog ou mais recente).
2. Deixe o Android Studio gerar o Gradle Wrapper automaticamente (ou use um
   Gradle 8.7+ já instalado) — os arquivos `gradle/wrapper/*` não foram incluídos.
3. Sync do Gradle → Run.

## Login de teste

- E-mail: `teste@patamar.app`
- Senha: `Teste@123`

(populado automaticamente no banco Room na primeira execução, via `MockDataSource`)

## O que está implementado

- Splash → Onboarding (3 slides) → Login/Cadastro → Filtros → Main (4 tabs)
- Login e cadastro reais (banco Room local), com toda a regra em `data/repository/AuthRepository`
  + `core/validation/AuthValidator` — a UI só mostra o que a camada de dados devolve:
  - e-mail normalizado (trim + minúsculas) e único (índice único no banco); nome, senha forte
    (8–64, maiúscula, minúscula, número, sem espaços), confirmação e termos
  - senha com PBKDF2-HmacSHA256 + salt aleatório (hashes SHA-256 antigos continuam válidos)
  - bloqueio de 30s após 3 tentativas erradas **por e-mail**, persistido (sobrevive a reabrir o app)
  - entrada por Perfil / Salvos (visitante) abre login ou cadastro direto
  - testes JVM: `./gradlew testDebugUnitTest` (validação, hash, cadastro, login, bloqueio)
  - ainda placeholder: "Esqueci a senha" e "Alterar senha" (precisam de e-mail/servidor real)
- Modo visitante (guest) — bloqueia a aba Salvos
- Mapa com OSMDroid (OpenStreetMap), 62 eventos mock (todas as categorias) com pins coloridos por categoria
- Newsletter bottom sheet (1x por sessão, 3s de delay)
- Explorar: busca com debounce, seções editoriais (em alta, perto de você, fim de
  semana, gratuitos), filtro por categoria
- Salvos: lista com swipe-to-delete, contagem em badge na bottom nav
- Perfil: dados do usuário, notificações (mock), logout
- EncryptedSharedPreferences para sessão e filtros
- ProGuard configurado, minify habilitado em release

## O que NÃO foi implementado (conforme escopo do beta)

Backend real, OAuth, push notifications reais, analytics, upload de foto,
clustering de mapa, pagamentos, CI/CD — todos marcados como `// TODO: produção`
onde relevante no código.

## Observação sobre fontes

A spec original pedia a fonte Inter como asset. Como o ambiente de geração não
tem acesso à internet para baixar o arquivo da fonte, o tema usa a fonte padrão
do sistema. Para usar Inter: baixe os arquivos .ttf em
https://fonts.google.com/specimen/Inter, coloque em `res/font/`, e referencie
via `fontFamily` no `themes.xml`.

## Mapa — trocando para MapLibre (Opção B)

A implementação ativa usa OSMDroid (Opção A), agora com tiles CartoDB Dark
Matter (ver "Patch v0.1.1"). Para trocar para MapLibre + OpenFreeMap, veja
`docs/MapFragment_OpcaoB_MapLibre.kt.reference` e os comentários em
`app/build.gradle.kts` — nesse caso, use o estilo
`https://tiles.openfreemap.org/styles/positron` ou um estilo escuro
equivalente pra manter a mesma pegada minimalista.

## Segurança — exceção de rede para fotos do Explorar

A spec original previa nenhuma chamada de rede além dos tiles do mapa. A
partir do patch v0.1.1, os cards do Explorar carregam uma foto de capa por
evento via Coil, usando `https://picsum.photos/seed/<id>/600/750` (sem API
key, sem billing, determinístico por evento). Nenhum dado do usuário é
enviado nessa chamada — só a seed do evento, que é pública. O banco Room
teve a coluna `imageUrl` adicionada em `Event` (versão 2, recriado via
`fallbackToDestructiveMigration`).

## Divisão de tarefas (conforme spec)

- **Mateus** (3): Tela boas-vindas, Newsletter, Mapa → `ui/onboarding`, `ui/map`
- **Edivan** (2): Tela Explorar, Tela Perfil → `ui/explore`, `ui/profile`
- **João** (2): Tela Login, Tela Cadastro → `ui/auth`
- **Gabriel** (2): Tela Filtro, Tela Inicial (Splash + Main) → `ui/filter`,
  `ui/splash`, `ui/main`
- **Base comum**: `core/`, `data/`, Gradle, Design System, Mock Data
