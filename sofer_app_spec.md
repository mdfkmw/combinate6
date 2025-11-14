# Șofer App – Specificație completă (Android + Backend + Sync + NFC)

## 1. Scopul aplicației
Aplicația „Șofer” este utilizată de șoferii companiei pentru gestionarea curselor, validarea abonamentelor prin NFC, operarea rezervărilor și emiterea biletelor. Funcționează online și offline, sincronizând datele cu backend-ul când conexiunea este disponibilă.

## 2. Funcționalități principale
- Login șofer prin ID numeric.
- Selectarea vehiculului din listă.
- Selectarea cursei (rută + oră).
- Sincronizarea rezervărilor pentru cursa curentă.
- Pornire/oprire îmbarcare.
- Validare automată NFC fără buton.
- Emitere bilete (doar dacă casa de marcat este conectată).
- Operare offline + sincronizare incrementală când reapare internetul.
- Statistici locale: bilete emise, validări abonamente, rezervări etc.
- Captură foto cu camera Hikvision pentru antifraudă.
- Raportări cursă curentă.
- Închidere zi (Z pe casa de marcat).

## 3. Flux general aplicație
1. Pornire aplicație → conectare la casă de marcat (opțional).
2. Login → șofer introduce ID numeric.
3. Selectare mașină.
4. Ecran principal (două tab-uri):
   - **Tab Administrare**
   - **Tab Operații**
5. Sync obligatoriu → descărcare rezervări cursă.
6. Pornire îmbarcare → backend setează `boarding_started = true`.
7. Validări NFC + rezervări + bilete.
8. Încheiere cursă.
9. Închidere zi.

## 4. Tab Administrare
### 4.1 Selectare cursă
- Șoferul alege cursa și ora.
- Se afișează rutele disponibile pentru operatorul șoferului.

### 4.2 Sincronizare rezervări
- Necesită internet.
- Trimite toate datele nesincronizate spre backend.
- Primește rezervările actualizate + starea cursei.
- După sincronizare, cursa este „pregătită pentru îmbarcare”.

### 4.3 Pornire îmbarcare
- Necesită internet.
- Blochează rezervările noi în backend.
- Permite aplicației să opereze offline fără risc de conflicte.

### 4.4 Reinițializare casă de marcat
- Reconfigurează casa dacă e deconectată.

### 4.5 Încheiere cursă
- Marchează cursa ca finalizată.

### 4.6 Rapoarte
- Raport cursă curentă.
- Raport validări abonamente.
- Raport debarcări.
- Raport sesiune curentă.

### 4.7 Închidere zi
- Trimite Z la casa de marcat.
- Termină sesiunea.

## 5. Tab Operații
### 5.1 Selectare stație curentă
- Mod automat (GPS).
- Mod manual (listă stații).

### 5.2 Emitere bilet
**Activ doar dacă:**
- casă conectată,
- cursă selectată,
- `boarding_started = true`,
- stație curentă validă.

Flow:
1. Șoferul introduce stația urcare → stația coborâre.
2. Prețul este calculat **local** folosind tabele descărcate.
3. Comandă locală la casa de marcat.
4. Inserare în `pending_tickets`.
5. Sync când reapare internetul.

### 5.3 Rezervări
- Listă rezervări din DB local.
- Refresh de la backend când există internet.

### 5.4 Validări abonamente NFC (nu e implementat momentan in proiect)
- Fără buton – aplicația ascultă NFC permanent.
- La detectarea cardului:
  - Dacă online → validare prin backend.
  - Dacă offline → salvează local „pending”.

### 5.5 Cameră Hikvision
- Telefonul devine hotspot.
- Camera se conectează automat.
- Pozele se salvează local în `pending_snapshots`.

### 5.6 Statistici
- nr. bilete emise
- nr. validări abonamente
- nr. invalidări
- nr. rezervări încă neîmbarcate

# 6 Sync Engine
- rulează automat la:
  - pornire aplicație
  - selectare cursă
  - prima conectare la internet
  - manual din „Sincronizează”
- procesează în ordinea:
  1. pending_tickets
  2. pending_pass_validations
  3. pending_snapshots
  4. pending_trip_events
- după succes: șterge sau marchează ca sincronizat.


## 8. Offline-first rules
- dacă nu există internet:
  - nu se poate începe îmbarcarea,
  - nu se pot descărca rezervări,
  - SE POT:
    - valida NFC (înregistrare locală)
    - emite bilete
    - salva poze
- la reconectare:
  - totul se sincronizează.

## 9. Status bar afișat permanent
- Șofer: ID + nume
- Mașină selectată
- Rută + oră
- Casa: conectată/neconectată
- GPS: OK / slab / off
- Internet: online/offline
- Baterie %


## 11. To Do (roadmap)
- validare abonamente offline (caching)
- preluare nume șofer din backend
- afișare locuri ocupate în timp real
- integrare casă fiscală reală
- adaptare cameră Hikvision PTZ
- optimizare sync incremental

## 12. Concluzie
Documentul definește toate funcționalitățile aplicației Șofer, incluzând fluxuri, ecrane, API-uri backend, module arhitecturale, reguli offline-first și cerințe hardware (NFC, GPS, casă fiscală, hotspot, cameră IP).
