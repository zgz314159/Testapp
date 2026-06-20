import os

base = r'C:\Users\zgz31\AndroidStudioProjects\Testapp\app\src\main'

deepseek_path = os.path.join(base, 'java', 'com', 'example', 'testapp', 'data', 'network', 'deepseek', 'DeepSeekApiService.kt')
spark_path = os.path.join(base, 'java', 'com', 'example', 'testapp', 'data', 'network', 'spark', 'SparkApiService.kt')

print("=== DeepSeek lines 105-135 ===")
with open(deepseek_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()
for i, line in enumerate(lines[104:135], start=105):
    print(f'{i}: {line}', end='')

print("\n=== Spark lines 85-115 ===")
with open(spark_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()
for i, line in enumerate(lines[84:115], start=85):
    print(f'{i}: {line}', end='')

print("\n=== Spark lines 135-155 ===")
for i, line in enumerate(lines[134:155], start=135):
    print(f'{i}: {line}', end='')

print("\n=== Spark lines 160-175 ===")
for i, line in enumerate(lines[159:175], start=160):
    print(f'{i}: {line}', end='')
