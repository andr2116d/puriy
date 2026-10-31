import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { PantallaDeBienvenida } from './PantallaDeBienvenida'

describe('PantallaDeBienvenida', () => {
  it('avisa que pronto estaremos disponibles', () => {
    render(<PantallaDeBienvenida />)

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(
      'Muy pronto estaremos disponibles.',
    )
  })

  it('muestra la marca con un texto alternativo', () => {
    render(<PantallaDeBienvenida />)

    expect(screen.getByRole('img', { name: 'Puriy' })).toBeInTheDocument()
  })
})
