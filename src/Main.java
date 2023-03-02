import java.util.*;

public class Main {

    public static final List<Thread> threads = new LinkedList<>();
    private static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final String letters = "RLRFR";
    private static final int length = 100;

    public static void main(String[] args) throws InterruptedException {


        //Создаю поток, который будет отслеживать работу других потоков
        Thread checker = new Thread(() -> {
            synchronized (sizeToFreq) {
                while (!Thread.interrupted()) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        System.out.println("Interrupt не сработал!");
                    }
                    int key = 0;
                    int value = 0;
                    for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                        if (entry.getValue() > value) {
                            value = entry.getValue();
                            key = entry.getKey();
                        }
                    }
                    System.out.println("Текущий лидер среди частот: " + key + ". " + value + " раз!");
                }
            }
        });

        //Запускаю его
        checker.start();

        //Создаю и запускаю 1000 потоков кладу их в список потоков
        for (int i = 0; i < 1000; i++) {
            Thread thread = new Thread(() -> {
                Random random = new Random();
                StringBuilder route = new StringBuilder();
                for (int y = 0; y < length; y++) {
                    route.append(letters.charAt(random.nextInt(letters.length())));
                }
                String str = route.toString();
                int count = 0;
                for (int j = 0; j < str.length(); j++) {
                    if (str.charAt(j) == 'R') {
                        count++;
                    }
                }
                synchronized (sizeToFreq) {
                    if (sizeToFreq.containsKey(count)) {
                        int temp = sizeToFreq.get(count);
                        sizeToFreq.put(count, temp + 1);
                    } else {
                        sizeToFreq.put(count, 1);
                    }
                    sizeToFreq.notify();
                }
            });
            thread.start();
            threads.add(thread);
        }

        //Жду когда потоки завершат свою работу. Interrupt срабатывает не всегда. Надо успеть,
        // чтобы мы не зашли в while иначе бесконечный цикл.
        for (int i = 0; i < sizeToFreq.size(); i++) {
            if (i == sizeToFreq.size() - 1) {
                threads.get(i).join();
                checker.interrupt();
            } else {
                threads.get(i).join();
            }
        }
    }

}


