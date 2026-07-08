import { defineManifest } from '@crxjs/vite-plugin'

export default defineManifest({
  manifest_version: 3,
  name: 'JobMatch AI',
  description: 'Adapta tu CV y carta de presentación a una oferta concreta usando IA.',
  version: '0.1.0',
  action: {
    default_popup: 'src/popup/index.html',
    default_title: 'JobMatch AI',
  },
  background: {
    service_worker: 'src/background/serviceWorker.ts',
    type: 'module',
  },
  content_scripts: [
    {
      matches: [
        'https://*.linkedin.com/*',
        'https://*.infojobs.net/*',
        'https://*.indeed.com/*',
      ],
      js: ['src/content/index.ts'],
      run_at: 'document_idle',
    },
  ],
  permissions: ['storage', 'activeTab', 'tabs', 'scripting'],
  host_permissions: ['http://localhost:8080/*'],
})
