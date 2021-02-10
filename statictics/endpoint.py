from fastapi import FastAPI
from pydantic import BaseModel
import main

# import logging

app = FastAPI()


# logging.basicConfig(level=logging.DEBUG)

@app.get("/")
async def read_root():
    return {"Hello": "World"}


@app.get("/all")
async def test():
    return main.open_csv()


@app.get("/single_user_single_day/raw/{date}/{user}")
async def single_user_single_day_raw(date, user):
    return main.single_user_single_day_raw(date, user)


@app.get("/single_user_single_day/{date}/{user}")
async def single_user_single_day(date, user):
    return main.single_user_single_day(date, user)


@app.get("/all_users_by_day")
async def all_users_by_day():
    return main.all_users_by_day()


@app.get("/all_users_for_day/{day}")
async def all_users_for_day(day):
    return main.all_users_for_day(day)


@app.get("/all_days_by_user")
async def all_days_by_user():
    return main.all_days_by_user()


@app.get("/all_days_for_user/{user}")
async def all_days_for_user(user):
    return main.all_days_for_user(user)
