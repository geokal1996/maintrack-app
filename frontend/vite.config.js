import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
  build: {
    rollupOptions: {
      output: {
        // Spame to teliko JavaScript se kommatia anti gia ena arxeio 790KB.
        //
        // Giati exei noima: oi vivliothikes (React, recharts) allazoun spania, eno o
        // dikos mas kodikas allazei se kathe enimerosi. An einai ola se ena arxeio,
        // kathe mikri allagi anagkazei ton browser na ksanakatevasei KAI tis vivliothikes.
        // Xorismena, o browser kratae ta vendor kommatia sti mnimi tou.
        //
        // Episis to "vendor-charts" (recharts + d3) einai to megalytero kommati kai
        // xreiazetai MONO stis selides me diagrammata - etsi den to katevazei kaneis
        // pou apla anoigei ti lista vlavon.
        manualChunks: {
          'vendor-react': ['react', 'react-dom', 'react-router-dom'],
          'vendor-charts': ['recharts'],
        },
      },
    },
  },
})
