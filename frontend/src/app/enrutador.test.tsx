import { RouterProvider, createMemoryHistory } from '@tanstack/react-router'
import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { crearEnrutador } from './enrutador'

function abrir(direccion: string) {
  const enrutador = crearEnrutador(createMemoryHistory({ initialEntries: [direccion] }))
  render(<RouterProvider router={enrutador} />)
  return enrutador
}

describe('enrutador', () => {
  it('muestra la bienvenida en el inicio', async () => {
    abrir('/')

    expect(await screen.findByRole('heading', { level: 1 })).toBeInTheDocument()
  })

  it('lleva al inicio cualquier dirección que no existe', async () => {
    const enrutador = abrir('/una-pagina-que-no-existe')

    expect(await screen.findByRole('heading', { level: 1 })).toBeInTheDocument()
    expect(enrutador.state.location.pathname).toBe('/')
  })
})
