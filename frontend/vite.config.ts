import { defineConfig } from 'vitest/config';

export default defineConfig({
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8443',
                changeOrigin: true,
            }
        }
    },
    test: {
        environment: 'jsdom',
        globals: true
    }
});
