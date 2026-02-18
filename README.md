# Proyecto Reserva de Pistas de Pádel  
Programación de Aplicaciones Telemáticas    

Autores: 
Bibiana Dorado Mateos
Jose Manuel Fernandez Freige
Guillermo Fuentes Gonzalez
Isabel Alonso Casas  

---

## Introducción

El objetivo de esta práctica, es el desarrollo de una aplicación telemática que tiene como objetivo la planificación, organización,  gestión y reserva de pistas de pádel. 

La aplicación desarrollada permite a los usuarios registrarse en el sistema, autenticarse, consultar la disponibilidad de pistas, realizar reservas y gestionar sus propias reservas. Asimismo, se contempla la figura del administrador, encargado de la gestión de pistas y del control global del sistema. De este modo, se han implementado distintos niveles de autorización y autenticación que garantizan un acceso seguro y coherente con los requisitos definidos. 

El backend se ha desarrollado siguiendo la estrategia API REST, donde el servidor simplemente genera los datos no la vista, y por tanto las respuestas se dan en formato JSON y no HTML. Además, el patrón de software para dividir la lógica de la aplicación ha sido (MVC: modelo, vista, controlador). Para implementar el backend, se ha hecho uso de Spring Boot, un framework de desarrollo que implementa muchas de las especificaciones de JAKARTA EE y añade ciertas características. 

---

## Records

### Pista
Representa una pista de Pádel, con su id Pista, nombre, ubicación, precio por hora, si se encuentra activa y la fecha de alta. 

---

### Reserva
 Representa la reserva de una pista por parte de un usuario. Incluye, el id Reserva, el id Usuario, el id Pista, fecha de la reserva, hora de inicio, duración en minutos, hora de fin, estado (activa o cancelada) y fecha de creación de la reserva. 

---

### Rol
Representa el rol USER o ADMIN, mediante id Rol, nombre Rol, descripción.

---

### Usuario
Representa un usuario con su id Usuario, su rol, fecha de registro del usuario, si se encuentra activo o dado de baja, datos personales (nombre, apellido, email, telefono), la contraseña que usará para loguearse en la aplicación y por último una lista de las reservas que ha realizado.

---

### ModeloError
Es una estructura común de respuesta para errores. Incluye el código HTTP, mensaje, ruta donde ocurre el error y timestamp. Permite también incluir una lista de errores de campo cuando se incumple @Valid.


---
## Clases

### AlmacenDatos
Clase central para almacenar datos en memoria usando HashMap. Contiene los mapas de usuarios, pistas y reservas. Aporta métodos de consulta útiles (por ejemplo búsqueda de usuario por email) para no duplicar lógica en el controlador. Simula una base de datos con @Service, para poder trabajar con @EnableMethodSecurity, ya que no se podía si no implementar, si nuestros datos se encontraban en un Hashmap en el controller.

---
### PadelControlador
Controlador REST principal (@RestController) que expone los endpoints del sistema. Está organizado por bloques : registro, users, courts, availability, reservations (en orden de los endpoints que facilitaba la información de la práctica). Aplica reglas de negocio (duplicados, solapes, permisos “admin o user”...) y usa el almacén como fuente de datos.

---

### ConfiguracionSeguridad

@Configuration @EnableMethodSecurity Centraliza la seguridad, principalmente para los endpoints de login y logout. Permitiendo registrarse sin loguearse, pero obligando al resto de endpoints a pasar primero por el login antes de permitir acceder a las funcionalidades. Habilita autorización por rol para que @PreAuthorize funcione correctamente.

---
### ControladorGlobalDeErrores: 
@RestControllerAdvice que transforma excepciones a respuestas consistentes usando record ModeloError. Gestiona: errores lanzados manualmente con ResponseStatusException, errores automáticos de @Valid (MethodArgumentNotValidException) y  JSON mal formado (HttpMessageNotReadableException) y errores no controlados (500). Los errores 403 de Security todavía no están gestionados por ModeloError pero sí implementados. 

---

### TareasProgramadas
Clase que contiene lógica automática periódica (con @Scheduled). Permite ejecutar tareas sin intervención del usuario, como recordatorios o avisos mensuales.

---

## Principales conocimientos aplicados

### Validaciones
Se usan anotaciones de Bean Validation en records (@NotNull, @NotBlank, @Positive, @Email) junto con @Valid en los endpoints.Cuando un request no cumple, Spring lanza automáticamente MethodArgumentNotValidException y se responde con 400, incluyendo errores por campo mediante ModeloError.FieldError. Luego estos errores se gestionan por ControladorGlobalDeErrores.

### Gestión de errores 
Los errores se unifican en el ControladorGlobalDeErrores, devolviendo siempre un cuerpo ModeloError. Con @ExceptionHandler los errores lanzados en el Controller ya sea mediante la propia lógica o lanzados con throw new ResponseStatusException son capturados y personalizados con el record ModeloError. 

### Seguridad, autenticación y autorización
Cualquier usuario debe estar autenticado para los endpoints del sistema, y ciertas operaciones quedan restringidas a administradores o a los propietarios del recurso.

Log in: La autenticación se gestiona con Spring Security mediante sesión. El login no se implementa como un método manual en el PadelControlador (se eliminó el endpoint “hecho a mano”), sino que lo procesa Spring Security. El cliente llama al endpoint de login configurado (/pistaPadel/auth/login) enviando credenciales.Spring Security valida esas credenciales usando el servicio configurado (UserDetailsService que busca al usuario por email en AlmacenDatos).Si las credenciales son correctas, Spring crea una sesión.A partir de ese momento, el cliente ya no necesita reenviar usuario y contraseña, el servidor reconoce la sesión.

Log out: El logout también lo gestiona Spring Security, normalmente en un endpoint configurado (/pistaPadel/auth/logout). El cliente llama al logout. Spring invalida la sesión. A partir de ese punto, cualquier petición a endpoints protegidos vuelve a dar 401 porque ya no existe sesión válida.

Una vez autenticado el usuario, Spring Security incorpora a la sesión su rol de USER o ADMIN. Para restringir operaciones se utiliza @PreAuthorize, por ejemplo: @PreAuthorize("hasRole('ADMIN')") en creación, edición o desactivación de pistas, o en listados globales. Esta validación se hace antes de ejecutar el método del controlador: Si el usuario no está autenticado da error 401.Si está autenticado pero no tiene el rol requerido da error 403. En algunos endpoints la práctica exige que pueda acceder un administrador, o el propio usuario propietario del recurso.Para estos casos se aplica una comprobación adicional usando el usuario autenticado obtenido desde Authentication:Se compara el id del usuario autenticado con el id del recurso solicitado (userId o idUsuario de la reserva). Si no es admin y tampoco es dueño, se devuelve 403.

### Tareas programadas
Se habilita scheduling (@EnableScheduling) para ejecutar procesos automáticos.  



## Funcionalidades para Usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | /pistaPadel/auth/register | Registro de usuario |
| POST | /pistaPadel/auth/login | Inicio de sesión |
| POST | /pistaPadel/auth/logout | Cierre de sesión |
| GET | /pistaPadel/auth/me | Obtener usuario autenticado |
| GET | /pistaPadel/users/{userId} | Obtener un usuario por id |
| PATCH | /pistaPadel/users/{userId}| Actualizar datos (nombre, teléfono…) |
| GET | /pistaPadel/courts | Listar pistas |
| GET | /pistaPadel/courts/{courtId} | Obtener detalle de pista |
| GET | /pistaPadel/availability | Consultar disponibilidad |
| GET | /pistaPadel/courts/{courtId}/availability | Disponibilidad de una pista |
| POST | /pistaPadel/reservations | Crear reserva |
| GET | /pistaPadel/reservations | Listar reservas propias |
| PATCH | /pistaPadel/reservations/{id} | Modificar reserva |
| DELETE | /pistaPadel/reservations/{id} | Cancelar reserva |

---

## Funcionalidades para Administradores

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /pistaPadel/users | Listar usuarios |
| GET | /pistaPadel/users/{id} | Obtener usuario |
| PATCH | /pistaPadel/users/{id} | Modificar usuario |
| POST | /pistaPadel/courts | Crear pista |
| PATCH | /pistaPadel/courts/{courtId} | Modificar pista |
| DELETE | /pistaPadel/courts/{courtId} | Desactivar pista |
| GET | /pistaPadel/admin/reservations | Listar todas las reservas |

---

## Funcionamiento General

El flujo típico del sistema es el siguiente:

1. El usuario se registra en el sistema.
2. Inicia sesión mediante el endpoint de autenticación.
3. Una vez autenticado, puede consultar pistas y disponibilidad.
4. Puede crear reservas si el horario está libre.
5. Puede modificar o cancelar sus propias reservas.
6. Si dispone de rol ADMIN, puede gestionar usuarios, pistas y reservas globales.
7. El sistema ejecuta tareas automáticas programadas de forma periódica.
