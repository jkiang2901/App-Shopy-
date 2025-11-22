# BanHangApp - Ứng dụng bán hàng Android

Ứng dụng bán hàng di động được phát triển với Kotlin và Android Studio, kết nối với MongoDB thông qua REST API.

## 🏗️ Kiến trúc ứng dụng

- **MVVM Architecture**: Model-View-ViewModel pattern
- **Retrofit**: Thư viện HTTP client để gọi API
- **RecyclerView**: Hiển thị danh sách sản phẩm
- **LiveData**: Quan sát dữ liệu theo thời gian thực
- **ViewModel**: Quản lý UI-related data
- **Material Design**: Giao diện hiện đại theo Google Material Design

## 📋 Yêu cầu hệ thống

- Android Studio Arctic Fox trở lên
- Android SDK API level 24 trở lên
- Kotlin 1.9+
- Java 8+

## 🚀 Cài đặt và chạy

### 1. Clone dự án
```bash
git clone <repository-url>
cd BanHangApp
```

### 2. Mở dự án trong Android Studio
- Mở Android Studio
- Chọn "Open an existing project"
- Chọn thư mục `BanHangApp`

### 3. Cấu hình kết nối API
Mở file `app/src/main/java/com/example/banhangapp/api/RetrofitClient.kt` và thay đổi `BASE_URL`:

```kotlin
private const val BASE_URL = "http://YOUR_SERVER_IP:8080/" // Thay đổi IP server của bạn
```

### 4. Build và chạy
- Chọn device/emulator
- Nhấn Run button (Shift + F10)

## 🔧 Backend API Server

Ứng dụng yêu cầu một backend server với các endpoints sau:

### Products API
- `GET /api/products` - Lấy danh sách tất cả sản phẩm
- `GET /api/products/{id}` - Lấy chi tiết sản phẩm theo ID
- `GET /api/products/category/{category}` - Lấy sản phẩm theo danh mục
- `POST /api/products/search` - Tìm kiếm sản phẩm

### Response format
```json
[
  {
    "_id": "string",
    "name": "string",
    "price": "number",
    "description": "string",
    "image": "string",
    "category": "string",
    "stock": "number"
  }
]
```

## 📱 Tính năng

- ✅ Hiển thị danh sách sản phẩm dạng grid
- ✅ Tìm kiếm sản phẩm theo tên
- ✅ Xem chi tiết sản phẩm
- ✅ Thêm sản phẩm vào giỏ hàng
- ✅ Material Design UI
- ✅ Responsive layout
- ✅ Error handling
- ✅ Loading states

## 🗂️ Cấu trúc thư mục

```
app/
├── src/main/
│   ├── java/com/example/banhangapp/
│   │   ├── api/                 # API interfaces và Retrofit client
│   │   ├── adapter/             # RecyclerView adapters
│   │   ├── models/              # Data models
│   │   ├── repository/          # Repository pattern
│   │   ├── viewmodel/           # ViewModels
│   │   └── MainActivity.kt      # Main activity
│   ├── res/
│   │   ├── layout/              # XML layouts
│   │   ├── drawable/            # Drawables và icons
│   │   ├── values/              # Strings, colors, themes
│   │   └── ...                  # Other resources
│   └── AndroidManifest.xml
├── build.gradle                 # App-level build configuration
└── ...                          # Other project files
```

## 🔌 Dependencies chính

- `androidx.appcompat:appcompat`
- `com.google.android.material:material`
- `androidx.constraintlayout:constraintlayout`
- `com.squareup.retrofit2:retrofit`
- `com.squareup.retrofit2:converter-gson`
- `androidx.lifecycle:lifecycle-viewmodel-ktx`
- `androidx.lifecycle:lifecycle-livedata-ktx`
- `androidx.recyclerview:recyclerview`
- `com.github.bumptech.glide:glide`

## 🐛 Debug và Troubleshooting

### Lỗi kết nối mạng
- Kiểm tra `INTERNET` permission trong AndroidManifest.xml
- Đảm bảo `usesCleartextTraffic="true"` cho HTTP connections
- Kiểm tra IP và port của server

### Build errors
- Clean và rebuild project: `Build -> Clean Project`, `Build -> Rebuild Project`
- Kiểm tra versions của dependencies

### Runtime errors
- Kiểm tra logcat trong Android Studio
- Đảm bảo server đang chạy và accessible

## 🔄 Tương lai phát triển

- [ ] Giỏ hàng và checkout
- [ ] User authentication
- [ ] Product details screen
- [ ] Categories filtering
- [ ] Offline mode
- [ ] Push notifications
- [ ] Payment integration

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
