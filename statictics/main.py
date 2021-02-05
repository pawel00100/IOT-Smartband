import csv
from typing import List
import datetime as dt
import matplotlib.pyplot as plt


def open_csv() -> List[dict]:
    file = open('aa.csv', 'r')
    return list(csv.DictReader(file))


def get_for_user(user: str, measurements: List[dict]) -> List[dict]:
    return [m for m in measurements if m['uid'] == user]


def get_for_day(day: str, measurements: List[dict]) -> List[dict]:
    return [m for m in measurements if day in str(m['time'])]


def steps_made_by_minmax(measurements: List[dict]) -> int:
    steps = [int(m['steps']) for m in measurements]
    return max(steps) - min(steps)


def sorted_mes(measurements: List[dict]) -> List[dict]:
    sorted_ = list(measurements)
    sorted_.sort(key=lambda s: s["time"])
    return sorted_


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


def kcal(steps: int, weight_in_kg) -> float:
    const = 0.00057119767  # calories per step per kg
    cal_per_step = const * weight_in_kg
    return cal_per_step * steps


def read_csv():
    measurements = open_csv()
    measurements = get_for_day("2021-02-02", measurements)
    measurements = get_for_user("user2", measurements)
    for row in measurements:
        print(row)

    print(steps_made_by_minmax(measurements))
    print(kcal(steps_made_by_minmax(measurements), 70))
    print('delta:' + str(steps_made_by_delta(measurements)))

    steps_chart(measurements, False, False)
    pulse_chart(measurements)


if __name__ == '__main__':
    read_csv()
