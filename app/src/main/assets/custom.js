var currentLocationIcon = L.icon({
  iconUrl: 'current.svg',
  iconSize: [40, 40],
  iconAnchor: [20, 20],
  popupAnchor: [0, -20]
});

var map = L.map('map',{
     center: [41.8719, 12.5674],
    zoomControl: false,
    attributionControl:false,
     maxBounds: [
        [35.0, 5.0],   // Sud-Ovest (vicino alla Sicilia e Sardegna)
        [47.5, 19.0]   // Nord-Est (vicino a Trieste e confini alpini)
      ],
      maxBoundsViscosity: 1.0
}).setView([45.4642, 9.1900], 13);


L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19,
  attribution: '&copy; OpenStreetMap contributors'
}).addTo(map);

marker = null;
currentPosition = new Array()

function updatePosition(lat, lon) {
    lat = parseFloat(lat);
    lon = parseFloat(lon);
  if (marker === null) {
    marker = L.marker([lat, lon],{icon:currentLocationIcon}).addTo(map);
      map.flyTo([lat,lon],13)
  } else {
    marker.setLatLng([lat, lon]);
  }
   currentPosition[0] = lat
   currentPosition[1] = lon
}
function goToCurrentPosition(){

    map.flyTo(currentPosition)
}
function posizionaFermate(fermate){
    var bounds = L.latLngBounds([]);
    fermate.forEach(function(fermata) {
        var latlng = L.latLng(fermata.latitude, fermata.longitude);
        L.marker(latlng)
            .addTo(map)
            .bindTooltip(fermata.stopName, {permanent: false, direction: "top"});
        bounds.extend(latlng); // <- AGGIORNA i bounds
    });
     map.fitBounds(bounds)
}

L.Control.GoToCurrent = L.Control.extend({
    onAdd: function(map) {
        // Crea il bottone
        const btn = L.DomUtil.create('button');
        btn.innerHTML =  `
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

        // Collega evento click
        btn.onclick = function() {
            goToCurrentPosition();
        };

        return btn;
    },

    onRemove: function(map) {

    }
});


L.control.goToCurrent = function(opts) {
    return new L.Control.GoToCurrent(opts);
}

L.control.goToCurrent({ position: 'topright' }).addTo(map);


