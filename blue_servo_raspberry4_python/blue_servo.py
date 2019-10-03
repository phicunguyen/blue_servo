#http://raspberry.io/projects/view/reading-and-writing-from-gpio-ports-from-python/
from bluetooth import *
import RPi.GPIO as GPIO
import time
import os
import sys

pwm = ""

def SetAngle(angle):
        global pwm
        duty = angle / 18 + 2
        GPIO.output(03, True)
        pwm.ChangeDutyCycle(duty)
        time.sleep(1)
        GPIO.output(03, False)
        pwm.ChangeDutyCycle(0)

def servo_init():
        global pwm
        GPIO.setwarnings(False)
        GPIO.setmode(GPIO.BOARD)
        GPIO.setup(03, GPIO.OUT)
        pwm=GPIO.PWM(03, 50)
        pwm.start(0)

def blue_servo():
        print ("Open RFCOMM")
        server_sock=BluetoothSocket( RFCOMM )
        server_sock.bind(("",PORT_ANY))
        server_sock.listen(1)

        port = server_sock.getsockname()[1]
        uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

        advertise_service( server_sock, "AquaPiServer",
                service_id = uuid,
                service_classes = [ uuid, SERIAL_PORT_CLASS ],
                profiles = [ SERIAL_PORT_PROFILE ])

        while True:
                print "Waiting for connection on RFCOMM channel %d" % port
                client_sock, client_info = server_sock.accept()
                print "Accepted connection from ", client_info

                try:
                        data = client_sock.recv(1024)
                        if data == '0':
                                SetAngle(0)
                        elif data == '90':
                                SetAngle(90)
                        elif data == '180':
                                SetAngle(180)
                        elif data == 'q':
                                pwm.stop()
                                GPIO.cleanup()
                        client_sock.send("!")

                except IOError:
                        print "bad connection"
                        pass
                    
                except KeyboardInterrupt:
                        print "disconnected"
                        client_sock.close()
                        server_sock.close()
                        print "all done"
                        break

if __name__ == '__main__':
        servo_init()
        blue_servo()
                    

