variable "nombre" {
  description = "Nombre base de los recursos"
  type        = string
}

variable "colas_de_errores" {
  description = "Nombre de cada DLQ"
  type        = map(string)
}
