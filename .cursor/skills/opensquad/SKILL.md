---
name: opensquad
description: Locates and loads the PsiPro multi-agent squad (PsiPro Hub) from the local novo-squad workspace. Use when the user mentions /opensquad, opensquad, squad PsiPro, PsiPro hub, or asks to open or continue the PsiPro delivery squad.
---

# Open Squad — PsiPro Hub

## Where the squad lives

The PsiPro squad **não está dentro do repo** `Psipro` (Android). Está no workspace **novo-squad** no Desktop:

| Item | Path (Windows, utilizador atual) |
|------|----------------------------------|
| **Raiz da squad PsiPro** | `%USERPROFILE%\Desktop\novo-squad\squads\psipro-hub` |
| Definição | `squad.yaml`, `squad-party.csv` |
| Pipeline | `pipeline/pipeline.yaml`, `pipeline/steps/`, `pipeline/data/` |
| Agentes | `agents/*.agent.md` (orchestrator, dev, qa, security, ux, product, finance-qa, etc.) |
| Memória / histórico de runs | `_memory/memories.md` |
| Briefing de tarefa | `pipeline/data/task-briefing.md`, `pipeline/data/execution-plan.md` |
| Saídas por run | `output/`, `output/YYYY-MM-DD-HHMMSS/` |

Se o projeto estiver clonado outro sítio, procurar por `squads/psipro-hub/squad.yaml`.

## O que fazer quando o utilizador invoca `/opensquad` ou “abre a squad PsiPro”

1. Confirmar que a pasta `psipro-hub` existe no caminho acima (ler `squad.yaml` ou `_memory/memories.md`).
2. Resumir em 2–4 linhas o propósito da squad (orquestração dev/QA/security/UX/produto para PsiPro), citando ficheiros-chave que leste.
3. Se a tarefa for **continuar trabalho**: ler `pipeline/data/task-briefing.md` e `_memory/memories.md` (últimas secções) para contexto atual; não expor credenciais de teste no chat — referir apenas “ver memórias, secção credenciais”.
4. Relacionar com código: **Android** → repo `Psipro`; **dashboard web** → `psipro-dashboard` (ex.: `.cursor/rules/psipro-autonomy.mdc`); **backend** → pasta `backend/` no mono-repo ou serviço Railway conforme memórias.

## Repositórios relacionados (fora da squad)

- `AndroidStudioProjects/Psipro` — app Kotlin
- `AndroidStudioProjects/psipro-dashboard` — dashboard web (se existir no disco)
- QA automatizado no mono-repo: `qa-psipro/`

## Notas

- Outras squads no mesmo `novo-squad` (ex.: `delivery-agents`, `plataforma-multiagente`, `sdr-ia-squad`) **não** são a squad PsiPro; usar só `psipro-hub` para pedidos PsiPro.
- Conteúdo em `_memory/` e relatórios em `output/` pode estar desatualizado face ao git — cruzar com o estado atual do repositório quando relevante.
