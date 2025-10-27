(() => {
    const map = L.map("map").setView([-3.73, -38.54], 13);

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19,
        detectRetina: true
    }).addTo(map);

    const markers = L.layerGroup().addTo(map);

    const delay = (ms) => new Promise(res => setTimeout(res, ms));
    const fetchJSON = (u) => fetch(u).then(r => {
        if (!r.ok) throw new Error(`${r.status} ${r.statusText}`);
        return r.json();
    });

    const processQueue = async (queue, limit, delayMs = 0) => {
        const results = new Array(queue.length);
        let index = 0;

        const worker = async () => {
            while (true) {
                const i = index++;
                if (i >= queue.length) break;
                try {
                    results[i] = await queue[i]();
                } catch (e) {
                    console.error("Erro na requisição:", e);
                }
                if (delayMs > 0) await delay(delayMs);
            }
        };

        await Promise.all(Array.from({ length: limit }, worker));
        return results;
    };

    const url = "https://zn4.m2mcontrol.com.br/api/forecast/lines/load";
    const only = [];
    const seenVehicles = new Set();

    const plotBus = (lineObj) => {
        const { busServiceNumber, codVehicle, destination, latLng } = lineObj;
        if (!only.includes(busServiceNumber)) return;
        if (!latLng || !latLng.lat || !latLng.lng) return;

        const vehicleKey = `${busServiceNumber}-${codVehicle}`;
        if (seenVehicles.has(vehicleKey)) return;
        seenVehicles.add(vehicleKey);

        L.marker([latLng.lat, latLng.lng])
            .addTo(markers)
            .bindPopup(`${busServiceNumber} (${codVehicle}) → ${destination}`);
    };

    const load = async () => {
        try {
            const allLines = await fetchJSON(`${url}/allLines/281`);

            const trajetos = [];
            for (const line of allLines) {
                if (only.length && !only.includes(line.numero)) continue;
                for (const t of line.trajetos || []) {
                    if (t?.startPoint?.id_migracao != null) {
                        trajetos.push(t);
                    }
                }
            }

            console.log(`Total de linhas recebidas: ${allLines.length}`);
            console.log(`Trajetos selecionados: ${trajetos.length}`);

            const bustopRequests = trajetos.map(t => () =>
                fetchJSON(`${url}/bustop/${t.id_migracao}/281`)
            );

            const bustopResults = await processQueue(bustopRequests, 6, 150);

            const uniqueStopIds = new Set();
            const fromPointRequests = [];

            for (const stops of bustopResults) {
                if (!Array.isArray(stops)) continue;
                for (const stop of stops) {
                    if (!stop?.id) continue;
                    if (uniqueStopIds.has(stop.id)) continue;
                    uniqueStopIds.add(stop.id);

                    fromPointRequests.push(() =>
                        fetchJSON(`${url}/forecast/lines/fromPoint/${stop.id}/281`)
                            .then(linesArr => {
                                if (!Array.isArray(linesArr)) return;
                                for (const lineObj of linesArr) {
                                    plotBus(lineObj);
                                }
                            })
                    );
                }
            }

            console.log(`Total de pontos únicos: ${uniqueStopIds.size}`);
            console.log(`Total de requisições fromPoint pendentes: ${fromPointRequests.length}`);

            await processQueue(fromPointRequests, 8, 150);
        } catch (err) {
            console.error("Erro ao carregar os dados:", err);
        }
    };

    const busNumberInput = document.getElementById("busNumber");
    const searchButton = document.getElementById("searchButton");
    const resultMessage = document.getElementById("resultMessage");

    searchButton.addEventListener("click", async () => {
        const busNumber = busNumberInput.value.trim();

        seenVehicles.clear();
        markers.clearLayers();
        only.length = 0;

        if (busNumber) {
            only.push(busNumber);
            resultMessage.textContent = "Carregando...";

            await load();

            resultMessage.textContent = `Encontrados ${seenVehicles.size} veículos`;
            console.log(`Total de veículos plotados: ${seenVehicles.size}`);
        }
    });
})();
