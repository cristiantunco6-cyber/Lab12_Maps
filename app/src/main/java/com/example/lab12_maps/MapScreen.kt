package com.example.lab12_maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import com.google.maps.android.compose.widgets.ScaleBar

@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val ArequipaLocation = LatLng(-16.4040102, -71.559611) // Arequipa, Perú
    val YuraLocation = LatLng(-16.2520984, -71.6836503)   // Yura
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ArequipaLocation, 12f)
    }

    // Mover la cámara automáticamente a Yura
    LaunchedEffect(Unit) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(YuraLocation, 12f), // Mover a Yura
            durationMs = 3000
        )
    }

    // Ejercicio 1: estado del tipo de mapa
    var tipoMapa by remember { mutableStateOf(MapType.NORMAL) }

    // Ejercicio 2: estado del permiso de ubicación
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Ejercicio 2: cliente de ubicación de Google
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Ejercicio 2: obtener la ubicación actual y mover la cámara hacia ella
    fun irAMiUbicacion() {
        if (!tienePermiso) return
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { ubicacion ->
                if (ubicacion != null) {
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(ubicacion.latitude, ubicacion.longitude), 16f
                            ),
                            1500
                        )
                    }
                }
            }
    }

    // Ejercicio 2: solicitar permisos de ubicación al usuario
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        tienePermiso = resultado[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                resultado[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (tienePermiso) irAMiUbicacion()
    }

    // Lista de puntos de interés
    val locations = listOf(
        LatLng(-16.433415, -71.5442652), // JLByR
        LatLng(-16.4205151, -71.4945209), // Paucarpata
        LatLng(-16.3524187, -71.5675994)  // Zamacola
    )

    // Coordenadas de los polígonos
    val mallAventuraPolygon = listOf(
        LatLng(-16.432292, -71.509145),
        LatLng(-16.432757, -71.509626),
        LatLng(-16.433013, -71.509310),
        LatLng(-16.432566, -71.508853)
    )

    val parqueLambramaniPolygon = listOf(
        LatLng(-16.422704, -71.530830),
        LatLng(-16.422920, -71.531340),
        LatLng(-16.423264, -71.531110),
        LatLng(-16.423050, -71.530600)
    )

    val plazaDeArmasPolygon = listOf(
        LatLng(-16.398866, -71.536961),
        LatLng(-16.398744, -71.536529),
        LatLng(-16.399178, -71.536289),
        LatLng(-16.399299, -71.536721)
    )

    // Coordenadas de la ruta (polilínea)
    val rutaCentroMall = listOf(
        LatLng(-16.399020, -71.536625), // Plaza de Armas
        LatLng(-16.422985, -71.530970), // Parque Lambramani
        LatLng(-16.432660, -71.509235)  // Mall Aventura
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Añadir GoogleMap al layout
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = tipoMapa,                // Ejercicio 1
                isMyLocationEnabled = tienePermiso // Ejercicio 2: punto azul
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = tienePermiso
            )
        ) {
            // Añadir marcador en Arequipa Perú
            Marker(
                state = rememberMarkerState(position = ArequipaLocation),
                icon = BitmapDescriptorFactory.fromResource(R.drawable.montana), // Icono personalizado
                title = "Arequipa, Perú"
            )

            // Añadir varios marcadores
            locations.forEach { location ->
                Marker(
                    state = rememberMarkerState(position = location),
                    title = "Ubicación",
                    snippet = "Punto de interés"
                )
            }

            // Dibujar polígonos
            Polygon(points = plazaDeArmasPolygon, strokeColor = Color.Red, fillColor = Color.Blue, strokeWidth = 5f)
            Polygon(points = parqueLambramaniPolygon, strokeColor = Color.Red, fillColor = Color.Blue, strokeWidth = 5f)
            Polygon(points = mallAventuraPolygon, strokeColor = Color.Red, fillColor = Color.Blue, strokeWidth = 5f)

            // Polilínea 1: ruta Plaza de Armas -> Lambramani -> Mall Aventura
            Polyline(points = rutaCentroMall, color = Color.Red, width = 10f)

            // Polilínea 2: línea punteada de Arequipa a Yura
            Polyline(
                points = listOf(ArequipaLocation, YuraLocation),
                color = Color.Magenta,
                width = 8f,
                pattern = listOf(Dash(30f), Gap(20f))
            )
        }

        // Ejercicio 1: botones para cambiar el tipo de mapa
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Normal" to MapType.NORMAL,
                "Satélite" to MapType.SATELLITE,
                "Híbrido" to MapType.HYBRID,
                "Terreno" to MapType.TERRAIN
            ).forEach { (nombre, tipo) ->
                Button(
                    onClick = { tipoMapa = tipo },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipoMapa == tipo) Color(0xFF1A73E8) else Color.White,
                        contentColor = if (tipoMapa == tipo) Color.White else Color.Black
                    )
                ) { Text(nombre) }
            }
        }

        // Ejercicio 2: botón para ir a la ubicación actual
        Button(
            onClick = {
                if (tienePermiso) irAMiUbicacion()
                else permisoLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(16.dp)
        ) { Text("📍 Mi ubicación") }

        // Puntos adicionales: barra de escala de Google Maps
        ScaleBar(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 64.dp, start = 12.dp),
            cameraPositionState = cameraPositionState
        )
    }
}