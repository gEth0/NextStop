# 📡 API e sistema di recupero fermate

NextStop utilizza una combinazione di codice nativo Android e funzioni JavaScript su WebView per mostrare e gestire le fermate del trasporto pubblico.

---

## 🌐 API usata

L'app interroga un endpoint RESTful (ad esempio TransitLand o una propria API proxy) per ottenere le fermate nella zona richiesta.

> 🔗 *Se stai usando TransitLand:*
GET https://transit.land/api/v2/rest/stops?lat=LAT&lon=LON&r=RAGGIO&per_page=100

> Oppure con nome fermata:
GET https://transit.land/api/v2/rest/stops?name=milano

---

## 📦 Classe `OttieniFermate`

Classe Java che gestisce il recupero asincrono delle fermate dal web.

### ▶️ Metodi principali

```java
public void esegui(String query);
```
Determina se la query è un nome o coordinate

Costruisce l’URL dinamicamente

Effettua la richiesta HTTP (con HttpURLConnection o OkHttp)

Esegue il parsing del JSON

Crea oggetti Fermata e li passa al listener

▶️ Callback Listener

interface Listener {
    void onFermateOttenute(ArrayList<Fermata> fermate);
    void onErrore(Exception e);
}
🔄 Comunicazione con WebView
La mappa interattiva è integrata tramite un file map.html contenente Leaflet.js.

Dopo aver ricevuto le fermate, l’app passa i dati al file HTML tramite:

```java
FunzioniHelper.passaFermateAJs(listaFermate, mappa);
Questo metodo converte le fermate in JSON e le invia a JavaScript con:
webView.evaluateJavascript("posizionaFermate(" + json + ")", null);
```

📜 JavaScript: map.html
Contiene le funzioni Leaflet per la mappa.

➤ posizionaFermate(fermate)
```javascript
function posizionaFermate(fermate) {
  clearMarkers(); // Pulisce i marker esistenti
  var bounds = L.latLngBounds([]);

  fermate.forEach(function(fermata) {
    var latlng = L.latLng(fermata.latitude, fermata.longitude);
    L.marker(latlng)
      .addTo(map)
      .bindTooltip(fermata.stopName, {permanent: false, direction: "top"});
    bounds.extend(latlng);
  });

  map.fitBounds(bounds);
}
```
➤ clearMarkers()
Funzione da implementare nel JS per rimuovere tutti i marker attivi dalla mappa, in modo da evitare duplicati dopo una nuova ricerca.

📍 Oggetto Fermata
Modello dati che rappresenta una fermata:

```java

class Fermata {
    private String stopName;
    private double latitude;
    private double longitude;
    // Getter e Setter...
}
```
📌 Altri dettagli tecnici
La connessione API è gestita in background per evitare blocchi UI.

È implementato un sistema di fallback in caso di errore di rete (onErrore()).

La progressDialog viene mostrata durante la fase di download dati.

🛠️ Possibili miglioramenti futuri
Caching locale delle fermate

Supporto multi-API (es. GTFS statico)

Ordinamento per distanza o nome


