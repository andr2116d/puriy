variable "nombre" {
  description = "Nombre base de los recursos"
  type        = string
}

variable "maximo_recepciones" {
  description = "Intentos antes de pasar a la DLQ"
  type        = number
  default     = 3
}

variable "visibilidad_segundos" {
  description = "Tiempo oculto de un mensaje en proceso"
  type        = number
  default     = 180
}
