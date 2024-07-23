import time
import face_recognition
import os
import cv2
import numpy as np
import math


def face_confidence(face_distance, face_match_threshold=0.6):
    range = (1.0 - face_match_threshold)
    linear_val = (1.0 - face_distance) / (range * 2.0)
    if face_distance > face_match_threshold:
        return str(round(linear_val * 100, 2)) + '%'
    else:
        value = (linear_val + ((1.0 - linear_val) * math.pow((linear_val - 0.5) * 2, 0.2))) * 100
        return str(round(value, 2)) + '%'
class Recognition:
    face_locations = []
    face_encodings = []
    face_names = []
    k_face_encodings = []
    k_face_names = []
    process_frame = True

    def __init__(self):
        self.encode()
    def encode(self):
        for image in os.listdir("Python_facial_recognition/model_1/faces"):
            face_image = face_recognition.load_image_file(f"Python_facial_recognition/model_1/faces/{image}")
            face_encoding = face_recognition.face_encodings(face_image)[0]
            self.k_face_encodings.append(face_encoding)
            name = os.path.splitext(image)[0]
            self.k_face_names.append(name)
    def run(self):
        video_capture = cv2.VideoCapture(0)
        if not video_capture.isOpened():
            sys.exit('Video not found')
        recognized_names = []
        start_time = None
        while True:
            _, frame = video_capture.read()
            if self.process_frame:
                small_frame = cv2.resize(frame, (0, 0), fx=0.25, fy=0.25)
                rgb_small_frame = small_frame[:, :, ::-1]
                self.face_locations = face_recognition.face_locations(rgb_small_frame)
                self.face_encodings = face_recognition.face_encodings(rgb_small_frame, self.face_locations)
                self.face_names = []
                for face_encoding in self.face_encodings:
                    matches = face_recognition.compare_faces(self.k_face_encodings, face_encoding)
                    name = "Unknown"
                    confidence = "0.0"
                    face_distances = face_recognition.face_distance(self.k_face_encodings, face_encoding)
                    best_match_index = np.argmin(face_distances)
                    if matches[best_match_index]:
                        name = self.k_face_names[best_match_index]
                        confidence = face_confidence(face_distances[best_match_index])
                        if name not in recognized_names:
                            recognized_names.append(name)
                            print(name)
                    self.face_names.append(f'{name} ({confidence})')
            self.process_frame = not self.process_frame
            for (top, right, bottom, left), name in zip(self.face_locations, self.face_names):
                top *= 4
                right *= 4
                bottom *= 4
                left *= 4
                cv2.rectangle(frame, (left, top), (right, bottom), (124, 252, 0), 2)
                cv2.rectangle(frame, (left, bottom - 35), (right, bottom), (124, 252, 0), cv2.FILLED)
                cv2.putText(frame, name, (left + 6, bottom - 6), cv2.FONT_HERSHEY_DUPLEX, 0.8, (0, 0, 0), 2)
            if self.face_names:
                if start_time is None:
                    start_time = time.time()
            cv2.imshow('Face Recognition', frame)
            if cv2.waitKey(1) == ord('q'):
                break
            if start_time is not None:
                elapsed_time = time.time() - start_time
                if elapsed_time >= 1:
                    print(name)
                    break



if __name__ == '__main__':
    fr = Recognition()
    fr.run()