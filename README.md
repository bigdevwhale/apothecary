# Apothecary

**Your fridge, cupboard and medicine box as a small potion shop.** («Зельевар»)

Every product is a bottle of essence that glows while it's fresh and dims as its date comes near.
The deadline is the date on the package or "opened + the days it keeps open", whichever comes first.
The freezer is the ice chest, where the clock stops and starts again, slower.

- **Shelves.** The cool cellar (fridge), the pantry, the ice chest (freezer) and the cabinet behind the
  counter (medicines and vitamins). A guide of about 170 products fills in how long each keeps, sealed and open.
- **The cauldron.** Cooking a meal is brewing a potion: tick what went in and the mix of essences makes one of
  40 recipes. Five essences make the Grand Elixir; three things saved on their last day make the Potion of the Last Hour.
- **Customers.** Six regulars come each morning and ask for exactly what is about to go to waste.
- **Coins and the shop.** Potions bring coins (more for things saved on their last day and for orders) that buy candles,
  herbs, a cat, a stained-glass window, a copper cauldron…
- **The guild.** Ranks from Apprentice to Guildmaster, judged by potions brewed and the share poured out in the last 30 days.
- **The ledger.** What was brewed and poured out, money lost, what goes to waste most, days without loss.
- **The calendar** shows on which day each ingredient runs out, and what was brewed or poured out on past days.
- **The cabinet** keeps the guild's kit (fever, pain, antiseptic, bandages, sorbent, allergy, heartburn, thermometer),
  warns a month ahead, counts vitamin courses, and sends expired medicines to a pharmacy, not the bin.
- **The morning bell** names what's on its last day today and tomorrow. Leftovers get a number to write on the lid.

Everything stays on the phone; no internet is needed.

## Build

```
./gradlew assembleDebug                 # app.apothecary.dev
./gradlew assembleDebug -Pdemo          # app.apothecary.demo with a shop ten weeks old
./gradlew testDebugUnitTest -Prender --tests '*RenderScreensRu*'   # screens to app/build/art
```
