# Șofer Android App

Acest modul conține o aplicație Android (Jetpack Compose) care implementează fluxul descris în `sofer_app_spec.md` pentru șoferi.

## Caracteristici
- Autentificare numerică pentru șofer.
- Selectarea vehiculului și a cursei dinamic sincronizate.
- Dashboard cu tab-uri „Administrare” și „Operații”.
- Motor de sincronizare incrementală pentru bilete, validări, poze și evenimente de cursă.
- Stocare locală Room pentru rezervări, bilete în așteptare, validări și evenimente.
- Funcționalități offline-first (emitere bilete, validare NFC, captură foto) cu sincronizare la reconectare.
- Bară de status permanentă cu informații despre șofer, conexiuni și stare dispozitiv.

## Stack tehnic
- Kotlin 1.9 + Jetpack Compose Material 3.
- Room pentru persistență locală.
- Coroutine + Flow pentru stare reactivă.
- Backend de test `FakeBackendApi` pentru rulare fără server extern.

## Rulare
1. Deschide proiectul `android/` în Android Studio (Giraffe sau mai nou).
2. Alege modulul `app` și rulează pe un emulator sau dispozitiv fizic cu Android 8.0+ (API 26).
3. Login de test: `1001` / `1002` / `2001`.

> **Notă:** Aplicația conține un backend fals pentru demo. Integrarea reală se poate face înlocuind `FakeBackendApi` cu implementarea API-ului existent.
