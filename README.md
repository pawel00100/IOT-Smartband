# IoT SmartBand

# Autorzy

- [Aleksandra Cynk][author1]
- [Paweł Kopel][author2]
- [Paweł Miziołek][author3]
- [Sebastian Wąwoźny][author4]

# Opis
Projekt za pomocą generatora, symulującego inteligentną opaskę, wysyła dane przy użyciu MQQT
do AWS Topic. Dane te są przetwarzane za pomocą Lambdy i zapisywane do tabeli w DynamoDB. Ponadto w przypadku niepokojących danych(ryzyko śmierci itp.) wysyłane są ostrzeżenia na e-mail za pomocą AWS SNS. Następnie kolejna lambda na podstawie aktualnych danych w tabeli dynamoDB tworzy plik csv ze wszystkimi danymi i umieszcza go to S3 Bucket. Plik ten jest wykorzystywany do generowania danych oraz wykresów za pomocą Pythona.

# Stack technologiczny

- Java
- Kotlin
- Python
- MQTT między opaską a backendem
- AWS IoT(IoT Core, DynamoDB, Lambda, S3)

# Szczegółowy opis
### 1. Generator

#### 1.1 Generowanie danych
Tutaj symulujemy opaskę. Przygotowane jest kilka „paczek” z parametrami odpowiadającymi np. treningowi, siedzeniu za 
biurkiem, spacerowi itp. Symulator losuje paczkę i czas do następnego losowania, następnie generuje losowe dane modulowane parametrami z paczki (np. trening - wyższe średnie tętno, siedzenie za biurkiem - niższe średnie tętno).

Użyty algorytm do liczenia kroków jest opisany w:
["A More Reliable Step Counter using Built-in Accelerometer  in Smartphone"](https://www.researchgate.net/publication/329526966_A_More_Reliable_Step_Counter_using_Built-in_Accelerometer_in_Smartphone)


Generator potrafi też tworzyć alarmy. Symulują one naciśnięcie przycisku na opasce w trudnej sytuacji.

Przykładowe użycie generatora:

```
[user@host] $ ./gradlew run --console=plain
...
Starting generator
> state
activity: walking
sex: female
> display
> Measurement(uid=user2, time=2021-02-10T22:09:53.891657, temp=30.59574963431126, pulse=68.29102777760463, steps=0)
> Measurement(uid=user2, time=2021-02-10T22:09:55.892072, temp=31.979125339233804, pulse=68.29102777760463, steps=0)
> Measurement(uid=user2, time=2021-02-10T22:09:57.892483, temp=31.983099196585847, pulse=65.6949898861198, steps=1)
> Measurement(uid=user2, time=2021-02-10T22:09:59.892870, temp=31.44123789025402, pulse=65.6949898861198, steps=1) 
> Measurement(uid=user2, time=2021-02-10T22:10:01.893272, temp=31.720433496139083, pulse=65.6949898861198, steps=1)
> Measurement(uid=user2, time=2021-02-10T22:10:03.893635, temp=31.13546903824449, pulse=65.6949898861198, steps=1)
> stop
> alarm
published alarm
> exit
Done
...
```


#### 1.2 Wysyłanie danych do AWS IoT
Dane z generatora są wysyłane za pomocą MQQT Client na AWS Topic `"/smartband"`

Połączenie AWS używa klas z przykładu dołączonego do AWS SDK (w folderze java). Program wysyła pomiary do AWS-u w 
formacie JSON z poniższymi parametrami:
* uid (unique user id)
* czas pomiaru (urządzenie jest połączone do internetu więc możemy założyć istnienie RTC)
* kroki, liczone od zera w momencie uruchomienia urządzenia. Oprogramowanie po stronie AWS powinno wyliczać delty pomiędzy pomiarami.
* puls
* temperatura

```kotlin
private suspend fun publish() = delayLoop(5000, { input != "stop" }) {
    measurement.mutex.withLock {
        measurement.time = LocalDateTime.now(ZoneOffset.UTC).toString()
        GenConfigProvider.saveMeasurement(measurement)
        val msg = GenConfigProvider.serialize(measurement)
        aws.publish(topic, msg)
    }
}
```
Alarmy zawierają
* uid
* czas pomiaru
* puls
* temperatura
* `"alarm"=true` - informacja o typie wiadomości

```kotlin
private fun alarm() {
    val alarm = GenConfigProvider.alarmFromMeasurement(measurement)
    val msg = GenConfigProvider.serialize(alarm)
    aws.publish(topic, msg)
    println("published alarm")
}
```

### 2. AWS IoT (BackEnd)

#### 2.1 Topic Rule
Ustawiono Topic Rule, tak aby dane, które są wysyłane z generatora na topic "/smartband" były triggerem dla Lambda function.

![generator](https://github.com/swawozny/test/blob/main/topicrule.png?raw=true)

#### 2.2 Lambda Function
W Lambda Function dane są przetwarzane i zapisywane do tabeli w DynamoDB za pomocą funkcji poniżej:

```python
import boto3
import csv

def lambda_handler(event, context):
    client = boto3.client('dynamodb')
    response = client.put_item(
    TableName='SmartData',
    Item={
        'pulse':{'N':str(event['pulse'])},
        'steps':{'N':str(event['steps'])},
        'temp':{'N':str(event['temp'])},
        'time':{'S':event['time']},
        'uid':{'S':event['uid']}
    }
    )
    return 0
```

#### 2.3 Destination Lambda Function

Utworzono także kolejną lambda function jako destination wcześniej utworzonej. Zadaniem nowej lambdy jest przetwarzanie danych z tabeli dynamoDB do pliku csv, który jest umieszczany na S3 bucket.
![awslambda](https://github.com/swawozny/test/blob/main/awslambda.png?raw=true)

Kod przetwarzający dane z tabeli dynamoDB do pliku csv na S3 Bucket:
```python
def lambda_handler(event, context):

    with open(TEMP_FILENAME, 'w') as output_file:
        writer = csv.writer(output_file)
        header = True
        first_page = True

        # Paginate results
        while True:
            # Scan DynamoDB table
            if first_page:
                response = table.scan()
                first_page = False
            else:
                response = table.scan(ExclusiveStartKey = response['LastEvaluatedKey'])

            for item in response['Items']:
                # Write header row?
                if header:
                    writer.writerow(item.keys())
                    header = False
                writer.writerow(item.values())

            # Last page?
            if 'LastEvaluatedKey' not in response:
                break

    # Upload temp file to S3
    s3_resource.Bucket(OUTPUT_BUCKET).upload_file(TEMP_FILENAME, OUTPUT_KEY)
```

#### 2.4 S3 Bucket

Efektem tego kodu jest powstanie takiego pliku na S3 Bucket:
![bucket](https://github.com/swawozny/test/blob/main/bucket.png?raw=true)

Z tego pliku bezpośrednio korzystamy do generowania szczegółowych danych oraz wykresów w pythonie.

#### 2.5 Powiadomienia o alarmach

Osobną lambdą (zawartą w folderze lambdas/mail sender) są odbierane alarmy z topicu. Na podstawie alarmów są wysyłane maile informujące np. operatora o potrzebie pomocy użytkownikowi.

Przykładowy mail:
>Alarm send by user2 at 2021-02-09 17:12:15
> 
>Pulse: 87
> 
>Temperature: 32.57

### 3. Opracowanie danych w Pythonie

Pobieramy dane z S3 Bucket za pomocą funkcji i przekształcamy je do listy słownikowej.
```python
def open_csv() -> List[dict]:
    url = 'URL_TO_CSV_FILE_ON_S3_BUCKET'
    r = requests.get(url, allow_redirects=True)

    open('data.csv', 'wb').write(r.content)
    file = open('data.csv', 'r')
    return list(csv.DictReader(file))
```

Dane przetwarzamy i wyciągamy te aktualne i dla konkretnego użytkownika:

```python
def read_csv():
    measurements = open_csv()
    measurements = get_for_day('2021-02-09', measurements)
    print(measurements)
    measurements = get_for_user('user2', measurements)

    print(steps_made_by_minmax(measurements))
    print(kcal(steps_made_by_minmax(measurements), 70))
    print('delta:' + str(steps_made_by_delta(measurements)))

    steps_chart(measurements, False, False)
    pulse_chart(measurements)
```

Na ich podstawie generujemy między innymi:
- Wykres odpowiadający pomiarowi pulsu
- Wykres odpowiadający pomiarowi kroków
- Liczba spalonych kcal
- Liczba kroków


#### 3.1 API REST
Stworzyliśmy API restowe wystawiające endpointy dla opracowanych danych

| endpoint                  | opis                   |
|:--------------------------|:------------------------------|
| `/all`              |wszystkie pomiary       |
| `/single_user_single_day/raw/{date}/{user}`        |wszystkie pomiary użytkownika w dany dzień                               |
| `/single_user_single_day/{date}/{user}`         |opracowane dane użytkownika w dany dzień                          |
| `/all_users_by_day`         |opracowane dane wszystkich użytkowników pogrupowane dniami                              |
| `/all_users_for_day/{day}`         |opracowane dane wszystkich użytkowników w dany dzień                                 |
| `/all_days_by_user`         |opracowane dane wszystkich dni pogrupowane użytkownikami                               |
| `/all_days_for_user/{user}`         |opracowane dane wszystkich dni danego użytkownika                                 |

opracowane dane zawierają:
- dzień, użytkownik lub brak (w zależności od endpointu, nie zwraca się danych podanych w zapytaniu i tworzących grupę)
- liczbę kroków
- liczbę spalonych kalorii

[author1]: <https://github.com/Latropos>
[author2]: <https://github.com/PKopel>
[author3]: <https://github.com/pawel00100>
[author4]: <https://github.com/swawozny>
