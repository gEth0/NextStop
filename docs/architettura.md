


### 📄 `architettura.md`

# Architettura dell'app

L’app è suddivisa in moduli principali, ognuno responsabile di una parte funzionale.

## 🌐 Mappa (`WebView` + JS)

- Visualizza la posizione utente
- Riceve i marker da Java
- Interagisce tramite bridge WebView

## 🔄 Timer (`AppState` + `ServizioTracciamento`)

- Salva lo stato del timer in `LiveData`
- Attiva un servizio foreground che monitora la posizione
- Al raggiungimento della fermata → invia notifica

## 📡 Permessi (`PermissionHelper`)

- Wrapper per richiedere e gestire permessi dinamici
- Mostra dialog personalizzati con `CustomAlertDialogs`

## 🧠 Stato globale (`AppState`)

- Singleton che contiene:
    - Timer attivo
    - Fermata selezionata
    - Istanza Vibrator
- Permette il collegamento tra servizi e activity

## 📦 Comunicazione

- Usa `BroadcastReceiver` per avvisare l’attività principale dell’arrivo
- Servizi in background → notifiche

