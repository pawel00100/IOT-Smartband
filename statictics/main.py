import csv
from typing import List
import datetime as dt
import matplotlib.pyplot as plt
import requests


def open_csv() -> List[dict]:
    url = 'https://testdataa.s3.amazonaws.com/smartdata.csv'
    r = requests.get(url, allow_redirects=True)

    open('data.csv', 'wb').write(r.content)
    file = open('data.csv', 'r')
    return list(csv.DictReader(file))


def get_for_user(user: str, measurements: List[dict]) -> List[dict]:
    return [m for m in measurements if m['uid'] == user]


def get_for_day(day: str, measurements: List[dict]) -> List[dict]:
    return [m for m in measurements if day in str(m['time'])]


def get_for_day_dt(day: str, measurements: List[dict]) -> List[dict]:
    return get_for_day(str(day), measurements)


def sorted_mes(measurements: List[dict]) -> List[dict]:
    sorted_ = list(measurements)
    sorted_.sort(key=lambda s: s["time"])
    return sorted_


def steps_made_by_minmax(measurements: List[dict]) -> int:
    steps = [int(m['steps']) for m in measurements]
    return max(steps) - min(steps)


# takes into count that counter may reset
def steps_made_by_delta(measurements: List[dict]) -> int:
    steps = [int(m['steps']) for m in sorted_mes(measurements)]
    sum = 0
    for i in range(len(steps) - 1):
        delta = steps[i + 1] - steps[i]
        delta = max(0, delta)
        sum += delta
    return sum


def steps_chart(measurements: List[dict], ignore_long_delay=False, steps_per_minute=False):
    measurements = sorted_mes(measurements)
    steps = [int(m['steps']) for m in measurements]
    datetimes = [str(m['time']) for m in measurements]
    datetimes = [dt.datetime.strptime(t[:-3], "%Y-%m-%dT%H:%M:%S.%f") for t in datetimes]
    new_datetimes = list()

    deltas = list()
    for i in range(len(steps) - 1):
        delta = steps[i + 1] - steps[i]
        delta = max(0, delta)

        time_delta = (datetimes[i + 1] - datetimes[i]).seconds

        if ignore_long_delay and time_delta >= 60:
            break

        if steps_per_minute:
            deltas.append(delta / time_delta * 60)
        else:
            deltas.append(delta)

        new_datetimes.append(datetimes[i + 1])

    plt.scatter(new_datetimes, deltas)
    plt.title("Steps")
    plt.show()


def pulse_chart(measurements: List[dict]):
    measurements = sorted_mes(measurements)
    pulses = [float(m['pulse']) for m in measurements]
    datetimes = [str(m['time']) for m in measurements]
    datetimes = [dt.datetime.strptime(t[:-3], "%Y-%m-%dT%H:%M:%S.%f") for t in datetimes]

    plt.scatter(datetimes, pulses)
    plt.title("Pulse")
    plt.show()


def kcal(steps: int, weight_in_kg: float) -> float:
    const = 0.00057119767  # calories per step per kg
    cal_per_step = const * weight_in_kg
    return cal_per_step * steps


def get_days(measurements: List[dict]) -> List[dt.datetime]:
    datetimes = [str(m['time']) for m in measurements]
    datetimes = [dt.datetime.strptime(t[:-3], "%Y-%m-%dT%H:%M:%S.%f").date() for t in datetimes]
    datetimes = list(set(datetimes))  # keep unique entries
    return datetimes


def get_users(measurements: List[dict]) -> List[str]:
    users = [str(m['uid']) for m in measurements]
    users = list(set(users))
    return users


def single_user_single_day(day: str, user: str):
    measurements = open_csv()
    measurements_for_day = get_for_day(day, measurements)
    measurements_for_user = get_for_user(user, measurements_for_day)
    steps = steps_made_by_delta(measurements_for_user)
    print(user + ": " + str(steps) + " steps,   " + str(kcal(steps, 70)) + " kcal")
    return {"steps": steps, "kcal": kcal(steps, 70)}


def single_user_single_day_raw(day: str, user: str):
    measurements = open_csv()
    measurements = get_for_day(day, measurements)
    return get_for_user(user, measurements)


def single_user_single_day_charts(day: str, user: str):
    measurements = single_user_single_day_raw(day, user)
    data = single_user_single_day(day, user)
    # for row in measurements:
    # print(row)

    print("steps by user in a day: " + str(steps_made_by_minmax(measurements)))
    print('same, but calculated by delta: ' + str(data["steps"]))
    print("kcal by user in a day: " + str(round(data["kcal"], 2)))

    steps_chart(measurements, False, False)
    pulse_chart(measurements)


def all_users_by_day():
    measurements = open_csv()
    days = get_days(measurements)

    results = {}
    for day in days:
        print(day)
        results[day] = all_users_for_day(day, measurements)
    return results


def all_users_for_day(day, measurements=open_csv()):
    measurements_for_day = get_for_day_dt(day, measurements)
    users = get_users(measurements_for_day)
    for user in users:
        measurements_for_user = get_for_user(user, measurements_for_day)
        steps = steps_made_by_delta(measurements_for_user)
        print(user + ": " + str(steps) + " steps,   " + str(kcal(steps, 70)) + " kcal")
        return {"user": user, "steps": steps, "kcal": kcal(steps, 70)}


def all_days_by_user():
    measurements = open_csv()
    users = get_users(measurements)

    results = {}
    for user in users:
        print(user)
        results[user] = all_days_for_user(user, measurements)
    return results


def all_days_for_user(user, measurements=open_csv()):
    measurements_for_user = get_for_user(user, measurements)
    days = get_days(measurements_for_user)
    for day in days:
        measurements_for_day = get_for_day_dt(day, measurements_for_user)
        steps = steps_made_by_delta(measurements_for_day)
        print(str(day) + ": " + str(steps) + " steps,   " + str(round(kcal(steps, 70), 2)) + " kcal")
        return {"day": day, "steps": steps, "kcal": kcal(steps, 70)}


if __name__ == '__main__':
    single_user_single_day_charts("2021-02-09", "user2")
    # all_users_by_day()
    # all_days_by_user()
