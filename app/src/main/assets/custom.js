// Icona per la posizione corrente
var currentLocationIcon = L.icon({
  iconUrl: 'current.svg',
  iconSize: [40, 40],
  iconAnchor: [20, 20],
  popupAnchor: [0, -20]
});

// Mappa Leaflet
var map = L.map('map', {
  center: [41.8719, 12.5674],
  zoomControl: false,
  attributionControl: false,
  maxBounds: [
    [35.0, 5.0],   // Sud-Ovest
    [47.5, 19.0]   // Nord-Est
  ],
  maxBoundsViscosity: 1.0
}).setView([45.4642, 9.1900], 13);

// Tile layer (OpenStreetMap)
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19,
  attribution: '&copy; OpenStreetMap contributors'
}).addTo(map);

// Posizione corrente
var marker = null;
var currentPosition = [];

// Marker delle fermate
var fermateMarkers = [];

// Aggiorna posizione corrente
function updatePosition(lat, lon) {
  lat = parseFloat(lat);
  lon = parseFloat(lon);

  if (marker === null) {
    marker = L.marker([lat, lon], { icon: currentLocationIcon }).addTo(map);
    map.flyTo([lat, lon], 13);
  } else {
    marker.setLatLng([lat, lon]);
  }

  currentPosition[0] = lat;
  currentPosition[1] = lon;
}

// Vai alla posizione corrente
function goToCurrentPosition() {
  if (currentPosition.length === 2) {
    map.flyTo(currentPosition);
  }
}

// Aggiunge le fermate sulla mappa
function posizionaFermate(fermate) {
  clearMarkers(); // Rimuovi marker esistenti

  var bounds = L.latLngBounds([]);
  fermate.forEach(function(fermata) {
    var latlng = L.latLng(fermata.latitude, fermata.longitude);
    var m = L.marker(latlng)
      .addTo(map)
      .bindTooltip(fermata.stopName, { permanent: false, direction: "top" });

    fermateMarkers.push(m); // Salva il marker
    bounds.extend(latlng);
  });

  if (!bounds.isEmpty()) {
    map.fitBounds(bounds);
  }
}

// Rimuove tutti i marker delle fermate
function clearMarkers() {
  fermateMarkers.forEach(function(marker) {
    map.removeLayer(marker);
  });
  fermateMarkers = []; // Svuota l'array
}

// Bottone per tornare alla posizione corrente
L.Control.GoToCurrent = L.Control.extend({
  onAdd: function(map) {
    const btn = L.DomUtil.create('button');
    btn.innerHTML = `
      <svg viewBox="0 0 24 24" width="20" height="20" fill="black" stroke="black" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
        <circle cx="12" cy="9" r="2.5" fill="white"/>
      </svg>`;
    btn.style.backgroundColor = 'white';
    btn.style.border = '2px solid gray';
    btn.style.borderRadius = '4px';
    btn.style.cursor = 'pointer';
    btn.style.padding = '4px 8px';
    btn.style.boxShadow = '0 1px 5px rgba(0,0,0,0.65)';
    L.DomEvent.disableClickPropagation(btn);

    btn.onclick = function() {
      goToCurrentPosition();
    };

    return btn;
  },
  onRemove: function(map) {}
});

// Aggiunge il controllo personalizzato alla mappa
L.control.goToCurrent = function(opts) {
  return new L.Control.GoToCurrent(opts);
};

L.control.goToCurrent({ position: 'topright' }).addTo(map);
