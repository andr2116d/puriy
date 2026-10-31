variable "nombre" {
  description = "Nombre base de los recursos"
  type        = string
}

variable "cidr" {
  description = "Rango de la VPC"
  type        = string
}

variable "subredes" {
  description = "Rangos de cada zona"
  type = map(object({
    publica    = string
    aplicacion = string
    datos      = string
  }))
}
