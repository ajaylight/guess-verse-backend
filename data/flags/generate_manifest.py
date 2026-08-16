import pandas as pd
from pathlib import Path

DATASET = Path(
    r"C:\Users\ajayl\Downloads\train-00000-of-00001.parquet"
)

OUT = Path("data/flags/flag-level-manifest.csv")

df = pd.read_parquet(DATASET)

# ============================================================
# EXPLICIT DIFFICULTY TIERS
# ============================================================

EASY = {
    "United States", "United Kingdom", "Canada", "Mexico",
    "Brazil", "Argentina", "France", "Germany", "Italy", "Spain",
    "Portugal", "Russia", "China", "Japan", "India", "Australia",
    "New Zealand", "South Korea", "Turkey", "Greece", "Switzerland",
    "Sweden", "Norway", "Denmark", "Finland", "Netherlands",
    "Belgium", "Ireland", "Austria", "Poland", "Ukraine",
    "South Africa", "Egypt", "Saudi Arabia",
    "United Arab Emirates", "Thailand", "Vietnam", "Indonesia",
    "Philippines", "Pakistan"
}

HARD = {
    "Afghanistan", "Albania", "Algeria", "Andorra",
    "Antigua and Barbuda", "Armenia", "Azerbaijan", "Bahamas",
    "Bahrain", "Barbados", "Belize", "Benin", "Bhutan", "Bolivia",
    "Bosnia and Herzegovina", "Botswana", "Brunei", "Burkina Faso",
    "Burundi", "Cabo Verde", "Central African Republic", "Chad",
    "Comoros", "Democratic Republic of the Congo", "Djibouti",
    "Dominica", "Dominican Republic", "Equatorial Guinea",
    "Eritrea", "Eswatini", "Fiji", "Gabon", "Gambia", "Georgia",
    "Ghana", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau",
    "Guyana", "Haiti", "Honduras", "Hungary", "Iceland", "Iran",
    "Iraq", "Jamaica", "Jordan", "Kazakhstan", "Kiribati",
    "North Korea", "Kuwait", "Kyrgyzstan", "Laos", "Latvia",
    "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein",
    "Lithuania", "Luxembourg", "Madagascar", "Malawi", "Maldives",
    "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius",
    "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro",
    "Mozambique", "Myanmar", "Namibia", "Nauru", "Nepal",
    "Nicaragua", "Niger", "Nigeria", "North Macedonia", "Oman",
    "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru",
    "Qatar", "Romania", "Rwanda", "Saint Kitts and Nevis",
    "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa",
    "San Marino", "São Tomé and Príncipe", "Senegal", "Seychelles",
    "Sierra Leone", "Singapore", "Slovakia", "Slovenia",
    "Solomon Islands", "Somalia", "South Sudan", "Sri Lanka",
    "Sudan", "Suriname", "Syria", "Tajikistan", "Tanzania",
    "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago",
    "Tunisia", "Turkmenistan", "Tuvalu", "Uganda", "Uruguay",
    "Uzbekistan", "Vanuatu", "Vatican City", "Venezuela",
    "Yemen", "Zambia", "Zimbabwe"
}

def base_difficulty(country):
    if country in EASY:
        return 1
    if country in HARD:
        return 3
    return 2


# ============================================================
# VALIDATE DATASET
# ============================================================

assert len(df) == 194, f"Expected 194 rows, got {len(df)}"
assert df["label"].nunique() == 194, "Duplicate labels found"
assert df["country_code"].nunique() == 194, "Duplicate country codes found"


# ============================================================
# SORT FLAGS BY BASE DIFFICULTY
# ============================================================

df["base_difficulty"] = df["label"].map(base_difficulty)

df = df.sort_values(
    ["base_difficulty", "label"],
    kind="stable"
).reset_index(drop=True)


# ============================================================
# SIX CONTROLLED REPEATS
#
# Each of these is allowed EXACTLY twice.
# No other country may repeat.
# ============================================================

REPEATS = [
    "Finland",
    "Canada",
    "Japan",
    "Brazil",
    "South Korea",
    "Switzerland",
]

assert len(REPEATS) == 6
assert len(set(REPEATS)) == 6

for country in REPEATS:
    assert country in set(df["label"]), (
        f"Repeat country not found in dataset: {country}"
    )


# ============================================================
# RESERVED REPEAT SLOTS
#
# Exactly one repeat in each selected level.
# ============================================================

REPEAT_SLOTS = {
    (4, 10): "Finland",
    (8, 10): "Canada",
    (12, 10): "Japan",
    (16, 10): "Brazil",
    (18, 10): "South Korea",
    (20, 10): "Switzerland",
}

assert set(REPEAT_SLOTS.values()) == set(REPEATS)


# ============================================================
# CREATE THE 194 UNIQUE SLOTS
# ============================================================

rows = []

unique_index = 0

for level in range(1, 21):

    for question_number in range(1, 11):

        # Reserve this slot for a controlled repeat.
        if (level, question_number) in REPEAT_SLOTS:
            continue

        row = df.iloc[unique_index]
        unique_index += 1

        if level <= 4:
            difficulty = "EASY"
        elif level <= 12:
            difficulty = "MEDIUM"
        else:
            difficulty = "HARD"

        rows.append({
            "level": level,
            "question_number": question_number,
            "country": row["label"],
            "country_code": row["country_code"],
            "difficulty": difficulty,
            "image_name": f'{row["country_code"]}.png',
            "repeat": False,
        })


assert unique_index == 194


# ============================================================
# ADD EXACTLY SIX CONTROLLED REPEATS
# ============================================================

for (level, question_number), country in REPEAT_SLOTS.items():

    row = df[df["label"] == country].iloc[0]

    if level <= 4:
        difficulty = "EASY"
    elif level <= 8:
        difficulty = "MEDIUM"
    elif level <= 12:
        difficulty = "MEDIUM"
    elif level <= 16:
        difficulty = "HARD"
    elif level <= 19:
        difficulty = "VERY_HARD"
    else:
        difficulty = "EXPERT"

    rows.append({
        "level": level,
        "question_number": question_number,
        "country": row["label"],
        "country_code": row["country_code"],
        "difficulty": difficulty,
        "image_name": f'{row["country_code"]}.png',
        "repeat": True,
    })


# ============================================================
# FINAL MANIFEST
# ============================================================

manifest = pd.DataFrame(rows)

manifest = manifest.sort_values(
    ["level", "question_number"]
).reset_index(drop=True)


# ============================================================
# HARD VALIDATION
# ============================================================

assert len(manifest) == 200

assert manifest["level"].nunique() == 20

assert manifest.groupby("level").size().eq(10).all()

assert manifest["repeat"].sum() == 6

assert manifest[~manifest["repeat"]]["country"].nunique() == 194

country_counts = manifest.groupby("country").size()

assert country_counts.max() == 2

assert (country_counts == 2).sum() == 6

assert set(
    country_counts[country_counts == 2].index
) == set(REPEATS)


# ============================================================
# WRITE CSV
# ============================================================

OUT.parent.mkdir(
    parents=True,
    exist_ok=True
)

manifest.to_csv(
    OUT,
    index=False
)


# ============================================================
# OUTPUT
# ============================================================

print()
print("======================================")
print(" FLAG ARENA MANIFEST GENERATED")
print("======================================")
print(f"Unique flags : {manifest['country'].nunique()}")
print(f"Total slots  : {len(manifest)}")
print(f"Levels       : {manifest['level'].nunique()}")
print(f"Repeats      : {manifest['repeat'].sum()}")
print(f"Output       : {OUT}")
print()

print("REPEATED FLAGS:")
print(
    manifest[
        manifest["country"].isin(REPEATS)
    ][
        ["level", "question_number", "country", "repeat"]
    ].to_string(index=False)
)

print()
print("QUESTIONS PER LEVEL:")
print(
    manifest.groupby("level").size().to_string()
)

print()
print("DIFFICULTY BY LEVEL:")
print(
    manifest.groupby(
        ["level", "difficulty"]
    ).size().to_string()
)