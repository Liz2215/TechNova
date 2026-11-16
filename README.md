# TechNova — Tienda virtual de gadgets (Android nativo)

Aplicación móvil nativa en **Kotlin + XML** para la venta de tecnología y accesorios inteligentes en Colombia.
Proyecto de la asignatura **Desarrollo de Aplicaciones Móviles Nativas** — Fundación Universitaria Compensar.

**Autora:** Lizeth Marcela Duitama Ramos · lmduitama@ucompensar.edu.co

> Entrega actual (**Proyecto final**): aplicativo **completamente funcional** conectado a **Firebase**
> (Authentication + Cloud Firestore), con biometría, CRUD de usuarios, productos (con foto desde cámara o galería),
> carrito y pedidos, **geolocalización** con las APIs de Google y **pasarela de pagos ePayco** (modo pruebas).

---

## Capturas

| Splash | Login (con huella) | Inicio Comprador | Pasarela de pago |
|---|---|---|---|
| ![](docs/capturas/splash.png) | ![](docs/capturas/login.png) | ![](docs/capturas/buyerHome.png) | ![](docs/capturas/checkout.png) |

| Panel Vendedor | Perfil Vendedor | Dashboard Admin | Reporte de ventas |
|---|---|---|---|
| ![](docs/capturas/sellerHome.png) | ![](docs/capturas/sellerProfile.png) | ![](docs/capturas/adminDashboard.png) | ![](docs/capturas/adminReports.png) |

Más pantallas en [`docs/capturas`](docs/capturas).

## Roles y pantallas

| Rol | Pantallas |
|---|---|
| **Transversal (todos)** | Splash screen · Inicio de sesión con selector de rol · **Autenticación con biometría** (huella/rostro con `BiometricPrompt`) · **Recuperación de contraseña** · Registro (autorización Ley 1581 y términos Ley 527) · Menú de navegación **Bottom Navigation** en cada rol |
| **Comprador** | Pantalla de inicio (promoción, categorías, más vendidos) · Catálogo con búsqueda y filtros · Categorías · Detalle de producto · Carrito · **Pasarela de pagos** (tarjeta con vista previa, PSE, Nequi, contraentrega) · Pedido confirmado · Perfil (pedidos, PQR, aviso de privacidad, eliminar cuenta) |
| **Vendedor** | Inicio (indicadores, gráfico semanal, pedidos recientes) · Panel de productos con **CRUD completo** · Historial de pedidos (filtro y cambio de estado) · **Perfil de vendedor** |
| **Administrador** | **Dashboard principal** · **Gestión de usuarios** (listado, creación, edición, eliminación) · **Gestión de productos** · **Reporte de ventas** (semana/mes/año, gráfico, más vendidos, ventas por categoría) · Ajustes generales |

**Navegación:** cada rol tiene su propio `BottomNavigationView` (secciones con *fragments*) y una barra superior
(`MaterialToolbar`) con menú de opciones (carrito y cerrar sesión).

## Backend y servicios (proyecto final)

| Componente | Cómo funciona |
|---|---|
| **Autenticación** | Firebase Authentication: registro, inicio y cierre de sesión, recuperación de contraseña por correo y acceso con Google. El rol (Comprador, Vendedor, Admin) se guarda en Firestore y decide qué panel se abre. |
| **Biometría** | `BiometricPrompt` + credenciales cifradas con el Android Keystore (`EncryptedSharedPreferences`). Funciona en celular físico con huella registrada. |
| **Base de datos** | Cloud Firestore con las colecciones `users` (y subcolección `cart`), `products`, `orders`, `settings` y `pqrs`. |
| **Fotos de productos** | Cámara (`TakePicture` + `FileProvider`) o galería (`PickVisualMedia`). La foto se reduce a 800 px, se comprime en JPEG y se guarda en Base64 dentro del producto en Firestore (el plan gratuito de Firebase ya no incluye Cloud Storage). |
| **Carrito y pedidos** | El carrito se guarda en `users/{uid}/cart`. Al confirmar, una **transacción** de Firestore valida y descuenta el stock y registra el pedido; el comprador ve su historial y el vendedor cambia el estado. |
| **Geolocalización** | Fused Location Provider (Google Play Services) + Geocoder: captura la ubicación del comprador, llena la dirección y guarda las coordenadas en el pedido (se pueden abrir en Google Maps). |
| **Pagos** | Checkout estándar de **ePayco** en modo pruebas dentro de un WebView. La app intercepta la respuesta (`ref_payco`) y confirma la transacción con el servicio de validación de ePayco antes de crear el pedido. También hay pago contraentrega. |
| **Seguridad** | Reglas de Firestore por rol (RBAC) en [`firestore.rules`](firestore.rules). R8 activado en *release*. |

### Primer uso

1. Registrar una cuenta desde la app.
2. Para tener un **administrador**, en la consola de Firebase → Firestore → `users` → documento del usuario, cambiar el campo `role` a `ADMIN`.
3. La primera vez que entra un administrador o vendedor con la base vacía, la app carga automáticamente los 8 productos de ejemplo.

## Estructura del proyecto

```
app/src/main/
├── java/com/ucompensar/technova/
│   ├── data/            Modelos, acceso a Firebase (Db) y repositorios: Auth, User, Product, Cart, Order, Settings, Pqr, ePayco
│   └── ui/
│       ├── auth/        SplashActivity, LoginActivity (biometría), RegisterActivity, ForgotPasswordActivity
│       ├── buyer/       BuyerActivity + fragments (inicio, catálogo, categorías, perfil), detalle, carrito, checkout
│       ├── seller/      SellerActivity + fragments (inicio, productos, pedidos, perfil), formulario de producto
│       ├── admin/       AdminActivity + fragments (dashboard, usuarios, productos, reportes, ajustes), formulario de usuario
│       └── common/      Formato COP, sesión, fotos (Base64), GPS, detalle de pedido, PDF del reporte, gráfico y adaptadores
└── res/
    ├── layout/          activity_*, fragment_*, item_* (filas de listas), layout_* (bloques reutilizables con <include>)
    ├── drawable/        ic_* (íconos vectoriales) y bg_* (fondos con <shape>: degradados, bordes, etiquetas)
    ├── menu/            menu_*_nav (menús inferiores), menu_toolbar_* y menu_order_status (menú emergente)
    ├── color/           selectores de color según estado (seleccionado / no seleccionado)
    ├── values/          colors, dimens, strings, styles, themes, arrays
    └── values-night/    colores para modo oscuro
```

## Decisiones de diseño (XML)

- **Paleta propia** (no se usan los colores por defecto): índigo `#2E2A6B`, menta `#12B886`, coral `#FF6B4A` para precios y ámbar `#FFB020` para calificaciones. Definida en `values/colors.xml`.
- **Modo oscuro** (RNF-03): `values-night/colors.xml` redefine los mismos nombres de color.
- **Recursos centralizados**: ningún layout tiene textos, colores ni medidas escritos directamente; todo sale de `strings.xml`, `colors.xml` y `dimens.xml`.
- **Estilos reutilizables** en `styles.xml`: botones (`Widget.TechNova.Button`, `.Accent`, `.Outlined`, `.Text`), tarjetas, campos de texto, barra superior, menú inferior y tipografías (`Text.TechNova.Title`, `.Body`, `.Price`, `.Badge`…).
- **Accesibilidad** (RNF-03): áreas táctiles mínimas de 48dp (`@dimen/touch_min`) y `contentDescription` en imágenes.
- **Material Design 3** como tema base (`Theme.Material3.DayNight.NoActionBar`).
- **ViewBinding** para acceder a las vistas desde Kotlin sin `findViewById`.
- **R8 / ProGuard** activado en la versión *release* (RNF-02).

## Cómo ejecutarlo

1. Clonar el repositorio y abrirlo con **Android Studio** (Koala o superior).
2. Esperar la sincronización de Gradle.
3. Ejecutar en un emulador o celular con Android 7.0 (API 24) o superior.
4. Se abre el splash y luego el login: crear una cuenta con **Regístrate** o entrar con una cuenta existente.
   Después del primer inicio con correo, se puede usar **Ingresar con huella**.
5. Pago de prueba en ePayco: usar las tarjetas de prueba que indica ePayco en su documentación (no se cobra dinero real).

**APK listo para validar:** en la sección [Releases](https://github.com/Liz2215/TechNova/releases) del repositorio.

**Generar el APK:** `Build > Build App Bundle(s) / APK(s) > Build APK(s)` o `./gradlew assembleDebug`
→ queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Imágenes

Las fotos de los productos están en `res/drawable-nodpi/` (formato WebP, livianas) y provienen de
[Unsplash](https://unsplash.com/license), de uso libre. Los íconos de categorías y de la interfaz son vectores propios en `res/drawable/`.

## Tecnologías

Kotlin 1.9 · Android Gradle Plugin 8.5 · compileSdk 34 · minSdk 24 · Material Components 1.12 · ConstraintLayout · RecyclerView · Fragments · ViewBinding · AndroidX Biometric · Corrutinas · Firebase Authentication · Cloud Firestore · Google Sign-In · Google Play Services Location · AndroidX Security (Keystore) · ePayco Checkout

## Requerimientos cubiertos

| Requerimiento | Dónde |
|---|---|
| RF-01 Autenticación | `SplashActivity`, `LoginActivity` (biometría), `RegisterActivity`, `ForgotPasswordActivity` |
| RF-02 Catálogo | `BuyerHomeFragment`, `CatalogFragment`, `BuyerCategoriesFragment`, `ProductDetailActivity` |
| RF-03 Carrito y pagos | `CartActivity`, `CheckoutActivity`, `PaymentActivity` (ePayco), `CartRepository`, `OrderRepository` |
| RF-04 Productos | `SellerProductsFragment`, `AdminProductsFragment`, `ProductFormActivity` |
| RF-05 Pedidos | `SellerDashboardFragment`, `SellerOrdersFragment`, `BuyerOrdersActivity` |
| RF-06 Administración | `AdminDashboardFragment`, `AdminUsersFragment`, `UserFormActivity`, `AdminReportsFragment`, `AdminSettingsFragment` |
| RF-07 Perfiles | `BuyerProfileFragment`, `SellerProfileFragment` |
| Geolocalización | `LocationHelper`, `CheckoutActivity`, `BuyerProfileFragment` (dirección con GPS) |
| RNF-02 Seguridad | `firestore.rules` (RBAC), `SecureStore` (Keystore), R8 |
| RNF-03 Usabilidad | Material 3, 48dp, modo oscuro |
