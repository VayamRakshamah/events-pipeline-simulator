# Deploy Frontend To GitHub Pages

The frontend is a static React/Vite app and can be deployed with GitHub Pages.

## Repository Settings

1. Enable GitHub Pages.
2. Set source to GitHub Actions.
3. Ensure the Pages workflow has `pages: write` and `id-token: write` permissions.

## API Base URL

Use the `api` query parameter to point the static dashboard at the hosted backend:

```text
https://vinaygupta.in/event-pipeline-simulator/?api=https://event-pipeline-simulator.onrender.com/api
```

For repository Pages paths, set:

```text
VITE_BASE_PATH=/event-pipeline-simulator/
```
