# 🎵 MoodSync - Facial Emotion Detection and AI-Driven Music Selection

MoodSync is an intelligent desktop application that detects a user's facial emotions in real time and recommends music based on the detected emotional state.

The system combines Computer Vision, Deep Learning, and Music Recommendation techniques to create a personalized and interactive music listening experience.

---

## 📌 Features

- Real-time webcam-based face detection
- Facial emotion recognition using Deep Learning
- Emotion classification:
  - Happy 😊
  - Sad 😔
  - Angry 😠
  - Surprise 😲
  - Fear 😨
  - Neutral 😐
  - Disgust 🤢
- Automatic music recommendation based on emotions
- Interactive GUI using Java Swing
- Local music playback support
- Online music recommendations through Spotify and YouTube
- Live camera feed visualization

---

## 🏗️ System Architecture

Webcam Input
↓
Face Detection (OpenCV)
↓
Face Preprocessing
↓
ONNX Emotion Recognition Model
↓
Emotion Prediction
↓
Music Recommendation Engine
↓
Music Playback & User Interface

---

## 🛠️ Technologies Used

### Programming Language
- Java

### Libraries & Frameworks
- OpenCV
- ONNX Runtime
- Java Swing
- JavaCV
- JLayer

### AI & Deep Learning
- CNN-based Emotion Recognition Model
- ONNX Model Format

### APIs
- Spotify API
- YouTube Integration

---

## 📂 Project Structure

```
MoodSync/
│
├── src/
│   ├── ui/
│   ├── emotion/
│   ├── music/
│   ├── camera/
│   └── main/
│
├── models/
│   └── emotion_model.onnx
│
├── music/
│
├── resources/
│
├── lib/
│
└── README.md
```

---

## ⚙️ Installation

### Prerequisites

- Java JDK 17 or later
- OpenCV
- ONNX Runtime
- Maven (Optional)
- Webcam

### Clone Repository

```bash
git clone https://github.com/yourusername/MoodSync.git
```

### Navigate to Project

```bash
cd MoodSync
```

### Run Application

```bash
java -jar MoodSync.jar
```

---

## 🚀 How It Works

1. Launch the application.
2. Allow webcam access.
3. The system detects the user's face.
4. Facial emotion is analyzed using the ONNX model.
5. Predicted emotion is displayed.
6. Music recommendations are generated automatically.
7. User can play recommended songs instantly.

---

## 📸 Screenshots

### Home Screen
(Add screenshot here)

### Emotion Detection
(Add screenshot here)

### Music Recommendation
(Add screenshot here)

---

## 📊 Future Enhancements

- Multiple face detection
- Playlist generation
- Mood tracking analytics
- Voice emotion recognition
- Mobile application version
- Cloud-based recommendation engine
- Personalized AI music assistant

---

## 🎯 Applications

- Mental wellness support
- Smart entertainment systems
- Personalized music recommendation
- Human-computer interaction research
- Educational AI projects

---

## 👨‍💻 Author

Guna Seelan

Master of Computer Applications (MCA)

Project Title:
MoodSync: Facial Emotion Detection and AI-Driven Music Selection

---

## 📜 License

This project is developed for educational and research purposes.

MIT License
