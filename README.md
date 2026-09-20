# Nyx Launcher

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Source: monorepo](https://img.shields.io/badge/source-monorepo-blue.svg)](https://github.com/reygnn/Kolibri-Launcher/tree/main/nyx)

A minimalist Android home-screen launcher. Early days.

> **This repository publishes Nyx's releases only** — grab a signed build from
> the [**Releases**](../../releases) page. The source code is **not** hosted
> here; see [Source](#source) below.

## What it is

Nyx is a stripped-down home screen: a clean home layout with folders and a
dock, plus a swipe-up app drawer with instant search. No widgets, no
"discover" tabs, no clutter.

It shares its architecture and conventions with its sister project
[Kolibri](https://github.com/reygnn/Kolibri-Launcher) — a Clean-Architecture
module split, Hilt for DI, Coroutines/Flow for async, and a JVM-first test
suite.

## Screenshots

<table>
  <tr>
    <td align="center">
      <img src="screenshots/home.png" width="280" alt="Nyx home screen — clock, dock and icon grid"><br>
      <sub>Home — clock, dock and icon grid</sub>
    </td>
    <td align="center">
      <img src="screenshots/drawer.png" width="280" alt="Nyx app drawer — instant search and folders"><br>
      <sub>App drawer — instant search and folders</sub>
    </td>
  </tr>
</table>

<sub>First run, fresh install (Android 16).</sub>

## Source

Nyx's source lives in the
**[Kolibri-Launcher monorepo](https://github.com/reygnn/Kolibri-Launcher)**
under [`nyx/`](https://github.com/reygnn/Kolibri-Launcher/tree/main/nyx),
alongside the shared `:core` / `:common-ui` / `:common-data` modules it builds
on. Build instructions, module layout and specs are there.

## License

GPL-3.0-or-later. Full license text and source: see the monorepo.
