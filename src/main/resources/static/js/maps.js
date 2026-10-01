// =========================================================
// SMARTDINE — Map Integration (Google Maps & Interactive Visuals)
// =========================================================

const smartMaps = {
    userLat: 12.9716, // Default Bengaluru coordinates
    userLon: 77.5946,
    hasUserLocation: false,
    mapInstance: null,
    markers: [],

    async requestUserLocation() {
        return new Promise((resolve) => {
            if (!navigator.geolocation) {
                showToast('Geolocation not supported by browser. Using default city location.', 'info');
                resolve({ lat: this.userLat, lon: this.userLon, acquired: false });
                return;
            }

            navigator.geolocation.getCurrentPosition(
                (pos) => {
                    this.userLat = pos.coords.latitude;
                    this.userLon = pos.coords.longitude;
                    this.hasUserLocation = true;
                    showToast('📍 Approximate location detected successfully!', 'success');
                    resolve({ lat: this.userLat, lon: this.userLon, acquired: true });
                },
                (err) => {
                    console.warn('Location permission denied or unavailable:', err.message);
                    showToast('Using default Bengaluru location for nearby discovery.', 'info');
                    resolve({ lat: this.userLat, lon: this.userLon, acquired: false });
                },
                { timeout: 7000, enableHighAccuracy: false }
            );
        });
    },

    initMap(containerId, establishments, onSelectEstablishment) {
        const container = document.getElementById(containerId);
        if (!container) return;

        // If Leaflet is loaded in HTML, initialize modern interactive map
        if (typeof L !== 'undefined') {
            if (this.mapInstance) {
                this.mapInstance.remove();
            }

            this.mapInstance = L.map(containerId).setView([this.userLat, this.userLon], 13);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap contributors | SmartDine'
            }).addTo(this.mapInstance);

            // User Location Marker
            const userIcon = L.divIcon({
                className: 'user-pin-icon',
                html: '<div style="background:#ff6b35; width:18px; height:18px; border-radius:50%; border:3px solid white; box-shadow:0 0 10px rgba(0,0,0,0.5);"></div>',
                iconSize: [24, 24]
            });

            L.marker([this.userLat, this.userLon], { icon: userIcon })
                .addTo(this.mapInstance)
                .bindPopup('<b>📍 Your Location</b><br>Searching hotels & canteens near you.');

            // Add Establishment Pins
            establishments.forEach(est => {
                const isHotel = est.type === 'HOTEL';
                const badgeColor = isHotel ? '#2563eb' : '#ea580c';
                const iconEmoji = isHotel ? '🏨' : '☕';

                const estIcon = L.divIcon({
                    className: 'est-pin-icon',
                    html: `<div style="background:${badgeColor}; color:white; padding:4px 8px; border-radius:12px; font-size:11px; font-weight:bold; box-shadow:0 3px 8px rgba(0,0,0,0.3); display:flex; align-items:center; gap:4px;">
                            ${iconEmoji} ${est.name.substring(0, 15)}...
                           </div>`,
                    iconSize: [120, 30]
                });

                const marker = L.marker([est.latitude, est.longitude], { icon: estIcon })
                    .addTo(this.mapInstance);

                const distanceText = est.distanceKm ? `${est.distanceKm} km away` : 'Nearby';
                const popupContent = `
                    <div style="font-family:'Plus Jakarta Sans',sans-serif; max-width:200px;">
                        <h4 style="margin:0 0 4px; font-size:14px; color:#1e293b;">${est.name}</h4>
                        <div style="font-size:12px; color:#64748b; margin-bottom:6px;">
                            <span style="color:#f59e0b;">★ ${est.rating || 4.2}</span> • 📍 ${distanceText}
                        </div>
                        <button onclick="window.selectEstablishmentFromMap(${est.establishmentId})" 
                                style="background:#ff6b35; color:white; border:none; padding:6px 12px; border-radius:14px; font-size:11px; font-weight:bold; cursor:pointer; width:100%;">
                            View Menu & Order
                        </button>
                    </div>
                `;

                marker.bindPopup(popupContent);
            });
        }
    },

    openExternalMap(lat, lon, name) {
        const url = `https://www.google.com/maps/search/?api=1&query=${lat},${lon}`;
        window.open(url, '_blank');
    }
};
