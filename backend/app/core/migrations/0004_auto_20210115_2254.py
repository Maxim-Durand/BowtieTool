# Generated by Django 2.2.17 on 2021-01-15 22:54

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('core', '0003_auto_20210115_1228'),
    ]

    operations = [
        migrations.AlterField(
            model_name='diagram',
            name='diagram_stat',
            field=models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='core.DiagramStat'),
        ),
    ]