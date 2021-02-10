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


def get_days(measurements: List[dict]) -> List[str]:
    valid_days = [str(m['__dt']) for m in measurements]
    valid_days = [t[0:10] for t in valid_days]
    my_set = set(valid_days)
    return sorted(list(my_set))


def is_next_day(day1: str, day2: str) -> bool:
    day1 = dt.datetime.strptime(day1, "%Y-%m-%d")
    day2 = dt.datetime.strptime(day2, "%Y-%m-%d")
    if day1 + dt.timedelta(days=1) != day2:
        return True
    return False


def get_day_diff(day1: str, day2: str) -> int:
    day1 = dt.datetime.strptime(day1, "%Y-%m-%d")
    day2 = dt.datetime.strptime(day2, "%Y-%m-%d")
    return (day2 - day1).days


def avg_temp_day(day: str, measurements: List[dict]) -> float:
    data = get_for_day(day, measurements)
    total_sum = 0
    counter = 0
    
    for row in data:
        total_sum += float(row['temp'])
        counter += 1
    
    return total_sum / counter


def avg_temp(measurements: List[dict]) -> dict:
    user_data = measurements
    temp_dict = {}
    
    prev_day = get_days(measurements)[0]
    str_prev_day = str(prev_day)[0:10]
    temp_dict[str_prev_day] = avg_temp_day(str_prev_day, user_data)
    
    for day in [day for day in get_days(measurements)[1:]]:
        
        str_day = str(day)[0:10]
        while prev_day + dt.timedelta(days=1) != day:
            temp_dict[str(prev_day + dt.timedelta(days=1))[0:10]] = 0
            prev_day = prev_day + dt.timedelta(days=1)
        
        temp_dict[str_day] = avg_temp_day(str_day, user_data)
        prev_day = day
    
    return temp_dict


def estimate_cycle_duration(o_days: List[str]) -> None:
    p_day = o_days[0]
    cycles_lengths = []
    
    for day in o_days:
        if (get_day_diff(p_day, day)) < 7:
            pass
        else:
            cycles_lengths.append(get_day_diff(p_day, day))
        p_day = day
    
    if len(cycles_lengths) > 0:
        cycle_len = sum(cycles_lengths) / len(cycles_lengths)
    else:
        cycle_len = 0
    if 20 < cycle_len < 50:
        print("Estimated average cycle lasts " + str(cycle_len) + " days")
    else:
        print("Unable to estimate cycle duration")


def temp_chart(measurements: List[dict]):
    data = avg_temp(measurements)
    fever = 35
    print(data)
    
    if len(data) < 2:
        return
    
    names = [str(day)[0:10] for day in list(data.keys())]
    
    values = list(data.values())
    # fever does not count in
    values_relevant = [v for v in values if fever > v > 0]
    fig, axs = plt.subplots(1, 1, figsize=(7, 6))
    plt.ylim(min(values_relevant) - .75, max(values) + .5)
    
    t_mean = sum(values_relevant) / len(values_relevant)
    # estimated ovulation BBT temp rise
    o_temp_rise = max((max(values_relevant) - t_mean) / 2, 0.35)
    
    clrs = ['#d66738' if x > fever else '#3ca657' if (x > t_mean + o_temp_rise) else '#95bfa0' if (
                x > t_mean + o_temp_rise / 2) else 'gray' for x in values]
    o_days = [names[x] for x in range(0, len(values)) if 30 > values[x] > t_mean + o_temp_rise / 2]
    if len(o_days) > 1:
        estimate_cycle_duration(o_days)
    
    plt.bar(names, values, color=clrs)
    fig.suptitle('Average temperature')
    plt.xticks(rotation='vertical')
    plt.show()


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

    # steps_chart(measurements, False, False)
    # pulse_chart(measurements)

    temp_chart(measurements)


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
