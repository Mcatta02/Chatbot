import sys
import cv2
import shutil

class ImageCapture:
    def __init__(self):
        self.cam = cv2.VideoCapture(0)
        cv2.namedWindow("capture user image")

    def capture_image(self, image_name):
        while True:
            ret, frame = self.cam.read()
            if not ret:
                print("Failed to capture an image!")
                break
            cv2.imshow("capture", frame)
            k = cv2.waitKey(1)
            if k % 256 == 32:
                image_name += ".jpg"
                cv2.imwrite(image_name, frame)
                destination = "Python_facial_recognition/model_1/faces/" + image_name
                shutil.move(image_name, destination)
                break

    def release_camera(self):
        self.cam.release()
        cv2.destroyAllWindows()

def main():
    image_name = sys.argv[1]  # Get the image name from command-line argument
    image_capture = ImageCapture()
    image_capture.capture_image(image_name)
    image_capture.release_camera()

if __name__ == "__main__":
    main()
