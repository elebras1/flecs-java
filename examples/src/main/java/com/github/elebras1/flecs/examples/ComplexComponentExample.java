package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.collection.LongList;
import com.github.elebras1.flecs.examples.components.Ideology;
import com.github.elebras1.flecs.examples.components.Minister;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ComplexComponentExample {

    public static void main(String[] args) {

        int N = 100_000;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        World world = new World();
        world.component(Minister.class);
        world.component(Ideology.class);

        long start = System.currentTimeMillis();
        LongList entityIds = new LongList();

        for (int i = 0; i < N; i++) {
            long id = world.entity();
            Entity e = world.obtainEntity(id);
            entityIds.add(id);

            int startDate = (int) LocalDate.parse("1950-01-01", dateFormatter).plusDays(i).toEpochDay();
            int endDate = startDate + 365;

            e.set(new Minister("Minister_" + i, "TAG_" + i, 1.0f, startDate, endDate));
            e.set(new Ideology(i % 256, (byte) (i % 100), (short) (i % 5000)));
        }

        long creationEnd = System.currentTimeMillis();

        for (long id : entityIds) {
            Entity e = world.obtainEntity(id);
            System.out.println(e.get(Minister.class));
            System.out.println(e.get(Ideology.class));
        }

        long end = System.currentTimeMillis();

        System.out.println("Creation of " + N + " entities: " + (creationEnd - start) + " ms");
        System.out.println("Creation + reads: " + (end - start) + " ms");

        world.close();
    }
}
