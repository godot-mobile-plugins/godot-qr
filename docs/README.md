<p align="center">
	<img width="256" height="256" src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/demo/assets/qr-android.png">
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	<img width="256" height="256" src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/demo/assets/qr-ios.png">
</p>

---

# <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="24"> Godot QR Plugin

Godot plugin that provides a unified GDScript interface for generating and scanning QR codes on **Android** and **iOS** using native platform implementations.

The plugin supports:

- Generating QR code images and textures
- Scanning QR codes from images
- Receiving scan results and errors via signals

**Key Features:**
- Unified GDScript API for Android & iOS
- Generate QR codes as Image or ImageTexture
- Custom foreground and background colors
- Scan QR codes from Image data
- Signal-based scan results and error handling
- Native platform performance

---

## <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="20"> Table of Contents
- [Installation](#installation)
- [Usage](#usage)
- [Signals](#signals)
- [Methods](#methods)
- [Classes](#classes)
- [Platform-Specific Notes](#platform-specific-notes)
- [Links](#links)
- [All Plugins](#all-plugins)
- [Credits](#credits)
- [Contributing](#contributing)

---

<a name="installation"></a>

## <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="20"> Installation
_Before installing this plugin, make sure to uninstall any previous versions of the same plugin._

_If installing both Android and iOS versions of the plugin in the same project, then make sure that both versions use the same addon interface version._

There are 2 ways to install the `QR` plugin into your project:
- Through the Godot Editor's AssetLib
- Manually by downloading archives from Github

### <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="18"> Installing via AssetLib
Steps:
- search for and select the `QR` plugin in Godot Editor
- click `Download` button
- on the installation dialog...
	- keep `Change Install Folder` setting pointing to your project's root directory
	- keep `Ignore asset root` checkbox checked
	- click `Install` button
- enable the plugin via the `Plugins` tab of `Project->Project Settings...` menu, in the Godot Editor

#### <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="16"> Installing both Android and iOS versions of the plugin in the same project
When installing via AssetLib, the installer may display a warning that states "_[x number of]_ files conflict with your project and won't be installed." You can ignore this warning since both versions use the same addon code.

### <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="18"> Installing manually
Steps:
- download release archive from Github
- unzip the release archive
- copy to your Godot project's root directory
- enable the plugin via the `Plugins` tab of `Project->Project Settings...` menu, in the Godot Editor

---

<a name="usage"></a>


## <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="20"> Usage
Add `QR` node to your main scene or register it as an AutoLoad singleton.

Generating a QR Code:
```gdscript
@onready var qr := $QR

func _ready():
	var img := qr.generate_qr_image(
		"https://godotengine.org",
		512,
		Color.BLACK,
		Color.WHITE
	)
	$TextureRect.texture = ImageTexture.create_from_image(img)
```

Generating a Texture directly:
```gdscript
var texture := qr.generate_qr_texture("Hello from Godot!")
$Sprite2D.texture = texture
```

Scanning a QR Code from an Image:
```gdscript
@onready var qr := $QR

func _ready():
	qr.qr_detected.connect(_on_qr_qr_detected)
	qr.qr_scan_failed.connect(_on_qr_qr_scan_failed)
	qr.scan_qr_image(my_image)


func _on_qr_qr_detected(data: String) -> void:
	print("QR detected: %s" % data)


func _on_qr_qr_scan_failed(error: ScanError) -> void:
	print("QR scan failed due to '%s'" % error.get_description())
```

### <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="20"> Scanning live camera feed
Combine the QR Plugin with the [Native Camera Plugin](https://github.com/godot-mobile-plugins/godot-native-camera) to scan QR codes directly from your device’s live camera feed. See the demo app for a working example.

---

<a name="signals"></a>

## <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="20"> Signals

- `qr_detected(data: String)` - Emitted when a QR code has been successfully scanned along with the scanned data
- `qr_scan_failed(error: ScanError)` - Emitted when scanning a QR code has failed


---

<a name="methods"></a>

## <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="20"> Methods

- `generate_qr_image(uri, size, foreground, background) -> Image`
	- Generates a QR code as a Godot Image.
	- `uri: String` – Data to encode
	- `size: int` – Output image size in pixels (default: 512)
	- `foreground: Color` – QR foreground color
	- `background: Color` – QR background color

- `generate_qr_texture(uri, size, foreground, background) -> ImageTexture`
	- Same as `generate_qr_image()`, but returns an ImageTexture.

- `scan_qr_image(image: Image) -> void`
	- Scans a QR code from a Godot Image.
	- Results are returned asynchronously via signals.

---

<a name="classes"></a>

## <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="20"> Classes

### <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="16"> ImageInfo

Internal data wrapper used for transferring image data between Godot and native code.

Properties (internal):
- `buffer: PackedByteArray`
- `width: int`
- `height: int`
- `format: Image.Format`
- `has_mipmaps: bool`

### <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="16"> ScanError

Encapsulates QR scanning errors.

Error Codes:
- `NONE`
- `INVALID_IMAGE`
- `NO_CODE_DETECTED`
- `SCANNER_FAILURE`
- `INTERNAL_ERROR`

Methods:
- `get_code() -> ScanError.Code`
- `get_description() -> String`

---

<a name="platform-specific-notes"></a>

## <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="20"> Platform-Specific Notes

### Android
- Download Android export template and enable gradle build from export settings
- **Troubleshooting:**
- Logs: `adb logcat | grep 'godot'` (Linux), `adb.exe logcat | select-string "godot"` (Windows)
- You may find the following resources helpful:
	- https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_android.html
	- https://developer.android.com/tools/adb
	- https://developer.android.com/studio/debug
	- https://developer.android.com/courses

### iOS
- Follow instructions on [Exporting for iOS](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_ios.html)
- View XCode logs while running the game for troubleshooting.
- See [Godot iOS Export Troubleshooting](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_ios.html#troubleshooting).

---

<a name="links"></a>

# <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="24"> Links

- [AssetLib Entry Android](https://godotengine.org/asset-library/asset/4582)
- [AssetLib Entry iOS](https://godotengine.org/asset-library/asset/4581)

---

# <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="24"> All Plugins

| Plugin | Android | iOS | Free | Open Source | License |
| :--- | :---: | :---: | :---: | :---: | :---: |
| [Notification Scheduler](https://github.com/godot-mobile-plugins/godot-notification-scheduler) | ✅ | ✅ | ✅ | ✅ | MIT |
| [Admob](https://github.com/godot-mobile-plugins/godot-admob) | ✅ | ✅ | ✅ | ✅ | MIT |
| [Deeplink](https://github.com/godot-mobile-plugins/godot-deeplink) | ✅ | ✅ | ✅ | ✅ | MIT |
| [Share](https://github.com/godot-mobile-plugins/godot-share) | ✅ | ✅ | ✅ | ✅ | MIT |
| [In-App Review](https://github.com/godot-mobile-plugins/godot-inapp-review) | ✅ | ✅ | ✅ | ✅ | MIT |
| [Native Camera](https://github.com/godot-mobile-plugins/godot-native-camera) | ✅ | ✅ | ✅ | ✅ | MIT |
| [Connection State](https://github.com/godot-mobile-plugins/godot-connection-state) | ✅ | ✅ | ✅ | ✅ | MIT |
| [OAuth 2.0](https://github.com/godot-mobile-plugins/godot-oauth2) | ✅ | ✅ | ✅ | ✅ | MIT |
| [QR](https://github.com/godot-mobile-plugins/godot-qr) | ✅ | ✅ | ✅ | ✅ | MIT |

---

<a name="credits"></a>

# <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="24"> Credits

Developed by [Cengiz](https://github.com/cengiz-pz)

Original repository: [Godot QR Plugin](https://github.com/godot-mobile-plugins/godot-qr)

---

<a name="contributing"></a>

# <img src="https://raw.githubusercontent.com/godot-mobile-plugins/godot-qr/main/addon/icon.png" width="24"> Contributing

See [our guide](https://github.com/godot-mobile-plugins/godot-qr?tab=contributing-ov-file) if you would like to contribute to this project.
