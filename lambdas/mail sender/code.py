import json

import smtplib
from socket import gaierror
import datetime as dt

port = 2525
smtp_server = "smtp.mailtrap.io"
login = "eb6f3fed7b9fd2"  # paste your login generated by Mailtrap
password = "cbbafa033c9bb3"  # paste your password generated by Mailtrap
sender = "from@example.com"
receiver = "mailtrap@example.com"

const = """\
Subject: Received alarm
To: {receiver}
From: {sender}

"""

last_send = dt.datetime(2000, 1, 1)


def send_mail(msg: str):
    try:
        with smtplib.SMTP(smtp_server, port) as server:
            server.login(login, password)
            server.sendmail(sender, receiver, msg)

        print('Sent')
    except (gaierror, ConnectionRefusedError):
        print('Failed to connect to the server. Bad connection settings?')
    except smtplib.SMTPServerDisconnected:
        print('Failed to connect to the server. Wrong user/password?')
    except smtplib.SMTPException as e:
        print('SMTP error occurred: ' + str(e))


def handle_json(msg):
    structure = json.loads(msg)

    global last_send
    current_time = dt.datetime.now()
    seconds_from_last_call = (current_time - last_send).seconds

    isAlarm = "alarm" in structure

    if (not isAlarm) or seconds_from_last_call < 60:
        return

    last_send = current_time

    time = dt.datetime.strptime(structure["time"][:-3], "%Y-%m-%dT%H:%M:%S.%f").strftime("%Y-%m-%d %H:%M:%S")
    message = const + "Alarm send by " + str(structure["uid"]) + " at " + time + \
              "\nPulse: " + str(int(structure["pulse"])) + \
              "\nTemperature: " + str(round(structure["temp"], 2))
    send_mail(message)



def lambda_handler(event, context):
    print(event)
    handle_json(json.dumps(event))
    return {
        'statusCode': 200,
        'body': json.dumps(event)

    }