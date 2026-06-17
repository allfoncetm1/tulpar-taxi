require('dotenv').config();
const express = require('express');
const path = require('path');
const app = express();

const MAPBOX_TOKEN = process.env.MAPBOX_TOKEN;
const PORT = process.env.PORT || 4000;

app.use(express.static(path.join(__dirname, 'public')));

// Передаём токен клиенту (Mapbox GL JS требует токен на клиенте для рендера тайлов)
app.get('/config', (req, res) => {
  res.json({ mapboxToken: MAPBOX_TOKEN });
});

// Прокси reverse geocoding — Mapbox
app.get('/geocode', async (req, res) => {
  const { lat, lng, q, limit = 5 } = req.query;
  try {
    const { default: fetch } = await import('node-fetch');
    if (lat && lng) {
      // Reverse: координаты → адрес
      const url = `https://api.mapbox.com/geocoding/v5/mapbox.places/${lng},${lat}.json?language=ru&access_token=${MAPBOX_TOKEN}`;
      const resp = await fetch(url);
      const data = await resp.json();
      const feature = data.features?.[0];
      if (feature) {
        const name = feature.place_name_ru || feature.place_name || '';
        // Берём только первые 2 части (улица, город)
        const short = name.split(',').slice(0, 2).join(',').trim();
        res.json({ hits: [{ name: short }] });
      } else {
        res.json({ hits: [] });
      }
    } else {
      // Forward: текст → координаты
      const bbox = '57.35,49.42,57.47,49.51'; // Кандыагаш viewbox lng_min,lat_min,lng_max,lat_max
      const url = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(q)}.json?language=ru&limit=${limit}&bbox=${bbox}&access_token=${MAPBOX_TOKEN}`;
      const resp = await fetch(url);
      const data = await resp.json();
      res.json(data);
    }
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

// Страницы карты
app.get('/', (req, res) => res.redirect('/citymap'));
app.get('/citymap', (req, res) => res.sendFile(path.join(__dirname, 'public', 'citymap.html')));
app.get('/order', (req, res) => res.sendFile(path.join(__dirname, 'public', 'order.html')));

app.listen(PORT, '0.0.0.0', () => {
  console.log(`tulpar-map запущен: http://0.0.0.0:${PORT} (доступен из эмулятора через 10.0.2.2)`);
});
