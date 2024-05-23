import os
import pandas as pd
import csv
import shutil

# Define constants
N = 45
FILENAME_TEMPLATE = "{}.txt"
FOLDER_PATH = r"C:\Users\emmanuel\Desktop\uni\Anaktisi Plhroforias\InformationFinal\src\main\resources\files2"

# Specify the file path to the CSV
csv_file = r"C:\Users\emmanuel\Desktop\uni\Anaktisi Plhroforias\InformationFinal\src\main\resources\files\papers.csv"

# Define the parameters for reading the CSV
kwargs = {
    'delimiter': ',',
    'quotechar': '"',
    'quoting': csv.QUOTE_ALL,
    'escapechar': '\\',
    'skipinitialspace': True,
    'encoding': 'latin-1'
}

# Load the CSV into a dataframe with error handling
try:
    txt_files_df = pd.read_csv(csv_file, **kwargs, on_bad_lines='skip')
    print("CSV loaded successfully.")
except pd.errors.ParserError as e:
    print("Error loading CSV:", e)
    raise

print("Columns in the CSV:", txt_files_df.columns.tolist())

# Select first N rows
txt_files_df = txt_files_df.iloc[:N]

# Delete previous folder if it exists
if os.path.exists(FOLDER_PATH):
    shutil.rmtree(FOLDER_PATH)

# Create new folder
os.makedirs(FOLDER_PATH)

# Iterate over each row of the DataFrame and write to file
for index, row in txt_files_df.iterrows():
    filename = FILENAME_TEMPLATE.format(row['title'])
    filepath = os.path.join(FOLDER_PATH, filename)

    # Prepare content for the file
    buffer = (
        f"Source ID: {row['source_id']}\n"
        f"Year: {row['year']}\n"
        f"Title: {row['title']}\n"
        f"Abstract: {row['abstract']}\n"
        f"Full Text: {row['full_text']}\n"
    )

    # Write content to the file
    with open(filepath, 'w', encoding='latin-1') as file:
        file.write(buffer)

print("Process finished successfully.")
