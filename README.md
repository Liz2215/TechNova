# TechNova — Tienda virtual de gadgets (Android nativo)

Aplicación móvil nativa en **Kotlin + XML** para la venta de tecnología y accesorios inteligentes en Colombia.
Proyecto de la asignatura **Desarrollo de Aplicaciones Móviles Nativas** — Fundación Universitaria Compensar.

**Autora:** Lizeth Marcela Duitama Ramos · lmduitama@ucompensar.edu.co

> Entrega actual (Actividad 4): **frontend completo de los 3 roles**. Los datos son de prueba
> (`FakeRepository`) y se reemplazarán por la API REST en la siguiente fase.

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

## Estructura del proyecto

```
app/src/main/
├── java/com/ucompensar/technova/
│   ├── data/            Modelos (Product, Order, User…) y FakeRepository (datos de prueba)
│   └── ui/
│       ├── auth/        SplashActivity, LoginActivity (biometría), RegisterActivity, ForgotPasswordActivity
│       ├── buyer/       BuyerActivity + fragments (inicio, catálogo, categorías, perfil), detalle, carrito, checkout
│       ├── seller/      SellerActivity + fragments (inicio, productos, pedidos, perfil), formulario de producto
│       ├── admin/       AdminActivity + fragments (dashboard, usuarios, productos, reportes, ajustes), formulario de usuario
│       └── common/      Formato de moneda (COP), cierre de sesión, gráfico de barras y adaptador de productos compartido
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
4. Se abre el splash y luego el login: escribir cualquier correo válido y una contraseña de 6+ caracteres, elegir el rol y entrar
   (o usar **Ingresar con huella** si el dispositivo tiene biometría configurada).

**APK listo para validar:** en la sección [Releases](https://github.com/Liz2215/TechNova/releases) del repositorio.

**Generar el APK:** `Build > Build App Bundle(s) / APK(s) > Build APK(s)` o `./gradlew assembleDebug`
→ queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Imágenes

Las fotos de los productos están en `res/drawable-nodpi/` (formato WebP, livianas) y provienen de
[Unsplash](https://unsplash.com/license), de uso libre. Los íconos de categorías y de la interfaz son vectores propios en `res/drawable/`.

## Tecnologías

Kotlin 1.9 · Android Gradle Plugin 8.5 · compileSdk 34 · minSdk 24 · Material Components 1.12 · ConstraintLayout · RecyclerView · Fragments · ViewBinding · AndroidX Biometric

## Requerimientos cubiertos en esta entrega

| Requerimiento | Dónde |
|---|---|
| RF-01 Autenticación | `SplashActivity`, `LoginActivity` (biometría), `RegisterActivity`, `ForgotPasswordActivity` |
| RF-02 Catálogo | `BuyerHomeFragment`, `CatalogFragment`, `BuyerCategoriesFragment`, `ProductDetailActivity` |
| RF-03 Carrito y checkout | `CartActivity`, `CheckoutActivity` |
| RF-04 Productos | `SellerProductsFragment`, `AdminProductsFragment`, `ProductFormActivity` |
| RF-05 Pedidos | `SellerDashboardFragment`, `SellerOrdersFragment` |
| RF-06 Administración | `AdminDashboardFragment`, `AdminUsersFragment`, `UserFormActivity`, `AdminReportsFragment`, `AdminSettingsFragment` |
| RF-07 Perfiles | `BuyerProfileFragment`, `SellerProfileFragment` |
| RNF-03 Usabilidad | Material 3, 48dp, modo oscuro |
