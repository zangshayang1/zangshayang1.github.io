---
layout: post
title:  OS concepts and implementations in C
date:   2016-10-14 01:08:00 -0800
categories: study-notes
tag: OS-in-C
---

* content
{:toc}



## malloc stimulation in C

CS 143 Extra Credit: Malloc and Free
The goal of this assignment is to implement your own malloc and free functions on a simulated disk, represented by a char array named disk.

``` java
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

int _DISK_CAPACITY = 100;

char disk[100];

typedef struct Xalloc_struct{
  int ptr;
  int size;
} Xalloc_struct;

void disk_init(){
//mark all disk space as free.
  int i;
  for (i = 0; i < _DISK_CAPACITY; ++i) {
    disk[i] = 0;
  }
}

Xalloc_struct* Xalloc(int size){
//allocate the appropriate space and return a pointer to the first byte of that space in disk.
// if there is not enough space, return NULL.
  int i = 0;
  while (i < _DISK_CAPACITY) {
    while (disk[i]) {
      i++;
    }
    int j = i;
    // printf("the first empty spot at %d\n", j);
    // see if we have this much space
    if (j + size >= _DISK_CAPACITY) {
          // printf("This allocation failed because there's not enough space.\n");
          return NULL;
        }

    int empty_count = 0;
    while (!disk[j] && empty_count < size) {
      empty_count++;
      j++;
    }

    if (empty_count == size) {
      // printf("haha\n");
      Xalloc_struct * x = (Xalloc_struct *)malloc(sizeof(Xalloc_struct));
      x->ptr = i; // points to the beginning of the chunk of memory taken
      x->size = size;
      int k;
      for (k = 0; k < x->size; k++) {
        disk[i + k] = 1;
      }
      // printf("allocation finished at %d\n", k);
      return x;
    }
    i = j; // if empty_count < size, meaning current slot is not big enough, check out the next one.
  }
  // printf("This allocation failed because there's not enough space.\n");
  return NULL;
}

int Xfree(Xalloc_struct* ptr){
//free the space pointed to by ptr.  if the pointer is NULL do nothing.  return 0 on success and 1 on an error.
  if (ptr == NULL) {
    return 1;
  }
  int i;
  for (i = 0; i < ptr->size; i++) {
    if (!disk[ptr->ptr + i]) {
      return 1;
    }
    disk[ptr->ptr + i] = 0;
  }
  return 0;
}

void print_disk_map(){
//print as a series of 0 and 1 the status of the disk, for example if the first three bytes are allocated and the rest are free print 1110000000000. . .

  int i;
  for (i = 0; i < _DISK_CAPACITY; i++) {
    printf("%d ", disk[i]);
    if (((i + 1) % 10) == 0) {
      printf("\n");
    }
  }
  return;
}
```
---

## Signal handling

Write a C program, called handle_signals, to do the following:(Remember that “^C” means pressing “Ctrl + C” on the keyboard)  
* wait in an infinite loop using the following main program:  
```
  int main() {
  // Turn off output buffering here if you like
  init_signal_handlers();
  while (1)
  sleep(1);
  }
```
* whenever the user types a ^C (interrupt) print an I (capital ‘i’), flush, DO NOT abort  
* whenever the user types a ^\ (quit) print a Q, flush, DO NOT quit  
* whenever the user types a ^Z (terminal stop) print an S, flush, DO NOT stop. On the third ^Z, print a summary of the number of signals received (starting with a newline so the characters printed above aren’t on the same line) exactly as follows, then exit:  
```
$ handle_signals
	^CI^CI^CI^CI^CI^\Q^\Q^ZS^ZS^ZS
  Interrupt: 5
  Stop: 3
  Quit: 2
```
You should flush the output after printing each character to prevent problems with buffered output or just turn off output buffering - your choice. Also, you may use signal() or sigaction(), but sigaction() is the preferred way to handle signals as it is the new and improved interface.

``` java
#include<stdio.h>
#include<signal.h>
#include<string.h>
#include<stdlib.h>
// memset() in string.h
// exit() in stdlib.h

struct sigaction act;
int c_count = 0;
int q_count = 0;
int z_count = 0;


void sigHandler(int signum, siginfo_t *info, void *ptr) {
 //printf("Received signal %d\n", signum);
 //printf("Signal originated from process %lu\n", (unsigned long)info->si_pid);

 switch (signum) {
  case SIGINT:
   printf("|");
   fflush(stdout);
   // when you call printf() sys will write the content into stdout buffer without displaying it in console
   // until you commit "\n" to let sys know that you finished this line.
   // An alternative to immediately display content in console is to use fflush(stdout), which flush out the content in buffer to the console;
   c_count++;
   break;
  case SIGQUIT:
   printf("Q");
   fflush(stdout);
   q_count++;
   break;
  case SIGTSTP:
   printf("S");
   fflush(stdout);
   z_count++;
   if(z_count == 3) {
    printf("\n");
    printf("Interrupt: %d\n", c_count);
    printf("Stop: %d\n", z_count);
    printf("Quit: %d\n", q_count);
    exit(-1);
   }
   break;
  default:
   break;
 }
}

int main() {

 memset(&act, 0, sizeof(act));
 // pass in a pointer act, init the memo block with 0

 act.sa_sigaction = sigHandler;
 act.sa_flags = SA_SIGINFO;

 sigaction(SIGINT, &act, NULL);
 sigaction(SIGQUIT, &act, NULL);
 sigaction(SIGTSTP, &act, NULL);
 // sigaction is both a mix data structure as well as a syscall function;

 while(1) {
  sleep(1);
  //printf("test\n");
 }
 return 0;
}

```
---

Write a C program, called send_signals, to do the following:  
* wait in an infinite loop for the signals SIGUSR1 or SIGINT...  
* in response to SIGUSR1 (hint: you’ll need to write your own test program to send this signal!), send SIGUSR2 to the process that sent your program SIGUSR1 (hint: you’ll have to figure out the sending process’s PID)
in response to SIGINT, print the number of SIGUSR1 signals received then exit.  

The output should follow this format exactly (e.g. if your test program sends 4 SIGUSR1 signals and then one SIGINT signal):  

```
$ send_signals
	Signals: 4
```

``` java
#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<signal.h>

struct sigaction act;
int sig_count = 0;

void sigHandler(int signum, siginfo_t *info, void *ptr) {

  switch (signum) {
    case SIGUSR1:
      sig_count++;
      int targetID = (int)info->si_pid;
      kill(targetID, SIGUSR2);
      break;
    case SIGINT:
      printf("Signals: %d\n", sig_count);
      exit(-1);
    default:
      break;
  }
    //printf("Originated from process %lu\n", (unsigned long)info->si_pid);
}

int main() {

  memset(&act, 0, sizeof(act));

  //printf("C process id: %d\n", getpid());
  act.sa_sigaction = sigHandler;
  act.sa_flags = SA_SIGINFO;

  sigaction(SIGUSR1, &act, NULL);
  sigaction(SIGINT, &act, NULL);

  while(1) {
    sleep(1);
  }
  return 0;
}

```
---

## Fork and My Shell

Write a program, called my_fork that uses fork() to create a total of 4 processes (3 calls to fork()) - each printing one letter of the alphabet from A to D K times.  K will be a command line argument, but should default to 10.  Have each process flush the output after printing each character with fflush(stdout).  

Be sure you do not leave any processes running.  You can check this with the command, ps -ef or ps aux. Note a parent process must wait() for each child after it has terminated, or that child will become a zombie.

Here is an example call (note the letters should be a jumbled mess and should be different order every time you call your program).
```
$ my_fork
	AAAAAABBBBBBBCDCDCDCDCDCDCBBBDCDCAAAADCD
$ my_fork 2
	ABCDBCDA
```

``` java
#include<stdio.h>
#include<unistd.h>
#include<stdlib.h>

int main(int argc, char **argv) {

  long k;
  int status = 0;

  if (argc > 1) {  
    char * s = argv[1];
    k = strtol(s, NULL, 10);
    //printf("%ld\n", k);
  } else {
    k = 10;
  }

  pid_t pid1 = fork();
  pid_t pid2 = fork();

  if (pid1 == 0) {
    if (pid2 == 0) {
      int i = 0;
      for (; i < k; i++) {
        printf("A");
        fflush(stdout);
      }
    } else {
      int i = 0;
      for (; i < k; i++) {
        printf("B");
        fflush(stdout);
      }    
    }
  }
  else {
    if (pid2 == 0) {
      int i = 0;
      for (; i < k; i++) {
        printf("C");
        fflush(stdout);
      }
    } else {
      int i = 0;
      for (; i < k; i++) {
        printf("D");
        fflush(stdout);
      }
    }
  }

  pid_t wpid;
  while ((wpid = wait(&status)) > 0) {
    ;
  }

  return 0;
}

```
---

Write a C program called my_shell, which is a step towards a simple command shell to process commands as follows:  
* print a prompt (“$ “), read a command on one line, and then evaluate it.  

To evaluate a command, you’ll need to fork a new process and then call some version of exec to execute the given command.  
* If the command is not recognized by the Operating System, print the appropriate error on one line and do not exit!  
* If the command is just a blank line (possibly containing spaces), you should not print anything and simply print another prompt and read another command as shown below.  
* Your shell should exit properly only when it reads an EOF character at the start of a command line.  

Consider the following example commands, but keep in mind that we’ll use different ones during testing:
```
$(BASH) my_shell
$ mkdir foo
$ touch foo/bar
$
$ mv foo baz
$ ls -1 baz
bar
$ ^D
$(BASH)
```

``` java
#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>
#include<string.h>
#include<sys/wait.h>


int TOK_BUFSIZE = 64;
char *ENV[] = {"TERM=xterm-256color", NULL};



int myshell_execute(char **argv) {

  if (argv[0] == NULL) {
    return 1;
  }

  pid_t pid, wpid;
  int status;

  pid = fork();

  if (pid == 0) {
    if (execvpe(argv[0], argv, ENV) == -1) {
      printf("Wrong Command!\n");
    }
  } else {

    do {
      wpid = waitpid(pid, &status,  WUNTRACED);
    } while (!WIFEXITED(status) && !WIFSIGNALED(status));
  }

  return 1;
}




char **myshell_splitline(char *line) {

  int bufsize = TOK_BUFSIZE, position = 0;
  char **tokens = malloc(bufsize * sizeof(char*));
  char *token;

  token = strtok(line, " ");
  while (token != NULL) {
    tokens[position] = token;
    position++;

    if (position >= bufsize) {
      bufsize += TOK_BUFSIZE;
      tokens = realloc(tokens, bufsize * sizeof(char*));
    }
    token = strtok(NULL, " ");
  }
  tokens[position] = NULL;
  return tokens;
}


char *myshell_readline() {

  char *line = NULL;
  ssize_t bufsize = 0;
  getline(&line, &bufsize, stdin);
  line[strcspn(line, "\n")] = 0;
  return line;
}




void myshell_loop() {

  char *line ;
  char **args;
  int status;

  do {
    printf("$ ");
    line = myshell_readline();
    args = myshell_splitline(line);
    status = myshell_execute(args);

    free(line);
    free(args);
  } while(status);

}




int main(int argc, char **argv) {
  myshell_loop();
  return 0;
}

```
---

## Threads and Mutex

Write a program to read in a list of numbers and compute their average.  
* Spawn an additional threads before reading from stdin.  
* Use a mutex (mutual exclusion) to ensure only one thread is actually reading stdin and aggregating the values at a given time.  

NOTE: don’t forget to join all your threads together before printing the results!  

NOTE: don’t worry about integer overflow or receiving fewer than 4 integers.  

NOTE: Because we are using mutexes, we cannot enforce the ordering of the threads’ execution. As such, it is possible that some threads will still do significantly more work than others.The import thing for this assignment is that each process has equal opportunity to enter the critical section and so some work.  

``` java
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <math.h>


int THR_NUM = 4;

typedef struct _thread_data_t {
	int tid;
	int count;
	double max;
	double min;
	double sum;
	pthread_mutex_t mutex;
} thread_data_t;

void * thread_function(void * arg) {
	thread_data_t * data = (thread_data_t *)arg;

	pthread_mutex_lock(&data->mutex);

	size_t bufsize = 32;
	size_t characters;
	char * buffer = (char *)malloc(bufsize * sizeof(char));
	// int count = 0;
	while ((characters = getline(&buffer, &bufsize, stdin)) != -1) {
		if (characters == 1) {
			// when '\n' is the only character of that line;
			//printf("Gotcha\n");
			continue;
		}
		double num = strtod(buffer, NULL);
		data->max = fmax(data->max, num);
		data->min = fmin(data->min, num);
		data->sum = data->sum + num;
		data->count++;
	}

	pthread_mutex_unlock(&data->mutex);

	pthread_exit(NULL);
}

int main(int argc, char const *argv[]) {

	pthread_t myThreads[THR_NUM];
	thread_data_t thread_data;
	pthread_mutex_t myMutex;
	pthread_mutex_init(&myMutex, NULL);

	thread_data.mutex = myMutex;
	thread_data.count = 0;
	thread_data.max = -pow(2.0, 31);
	thread_data.min = pow(2.0, 31) - 1;

	int rc;
	for (int i = 0; i < THR_NUM; ++i) {
		thread_data.tid = i;
		if ((rc = pthread_create(&myThreads[i], NULL, thread_function, &thread_data))) {
			fprintf(stderr, "error: pthread_create, rc: %d\n", rc);
			return EXIT_FAILURE;
		}
	}

	for (int i = 0; i < THR_NUM; ++i) {
		pthread_join(myThreads[i], NULL);
	}

	printf("max: %g\n", thread_data.max);
	printf("min: %g\n", thread_data.min);
	printf("average: %g\n", thread_data.sum / thread_data.count);

	return EXIT_SUCCESS;
}
```
---

## Circular buffer

The following program implements a circular buffer queue while the main program that calls it is left off from here. What the program will do is:  

A total of 2 consumer threads will be created to read one line at a time from the buffer, search the line for the fixed word, and increment a shared counter if it has been found. After all files have been fully read through and the circular buffer is empty, the main thread prints the final count of occurrences of the given word and then the program terminates.  

Note you will see errors due to race conditions. You will need add mutex to protect critical section.


``` java
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include "que.h"

////////////////////////////////////////////////////////////////
// The following is included in que.h                         //
////////////////////////////////////////////////////////////////

/*
// this is a circular buffer implementation of queue. You can see about it here: http://www.csanimated.com/animation.php?t=Circular_buffer
// Note: one slot must be left unused for this implementation

#define QUE_MAX 10

typedef struct Buffer_struct {
    char string[BUFSIZ];
} Buffer;


#define ELE Buffer



void add_match();
void report_matches(char *pat);
int que_init();
void que_error(char *msg);
int que_is_full();
int que_is_empty();
void que_enq(ELE v);
ELE que_deq();
*/
////////////////////////////////////////////////////////////////
// que.h stops here                                           //
////////////////////////////////////////////////////////////////

static ELE _que[QUE_MAX];
static int _front = 0, _rear = 0;
extern int producers_working;
static int matches = 0;


// extern pthread_mutex_t mutex;
// extern pthread_cond_t que_not_full;
// extern pthread_cond_t que_not_empty;

pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t que_not_full = PTHREAD_COND_INITIALIZER;
pthread_cond_t que_not_empty = PTHREAD_COND_INITIALIZER;


void add_match()
{
    //Note: you will need to lock this update because it is a race condition
    pthread_mutex_lock(&mutex);
    matches++;
    // printf("match -> %d\n", matches);
    pthread_mutex_unlock(&mutex);
}

void report_matches(char *pattern)
{
    printf("Found %d total matches of '%s'\n", matches, pattern);
}

int que_init()
{
}

void que_error(char *msg)
{
    fprintf(stderr, "***** Error: %s\n", msg);
    // exit(-1);
}

int que_is_full()
{
    return (_rear + 1) % QUE_MAX == _front; /* this is why one slot is unused */
}

int que_is_empty()
{
    return _front == _rear;
}

void que_enq(ELE v)
{
    // replace this spin with something better.....
    // while (que_is_full()) // note this is not right
    //     ;
    pthread_mutex_lock(&mutex);

    while (que_is_full()) {
        pthread_cond_wait(&que_not_full, &mutex);
    }


    if (que_is_full()) {
        que_error("enq on full queue");
    }

    // printf("ENQUE: _rear -> %d; _front -> %d\n", _rear, _front);

    _que[_rear++] = v;
    if (_rear >= QUE_MAX) {
        _rear = 0;
    }

    pthread_mutex_unlock(&mutex);
    pthread_cond_signal(&que_not_empty);   
}

ELE que_deq()
{
    // replace this spin with something better.....
    // while (producers_working && que_is_empty()) // note this is not right
    //     ;
    pthread_mutex_lock(&mutex);

    while (producers_working && que_is_empty()) {    
        pthread_cond_wait(&que_not_empty, &mutex);
    }

    // printf("%d\n", producers_working);
    if (producers_working && que_is_empty()) {
        que_error("deq on empty queue");
    }

    // printf("%d\n", producers_working);
    // printf("DEQUE: _rear -> %d; _front -> %d\n", _rear, _front);

    ELE ret = _que[_front++];
    if (_front >= QUE_MAX) {
        _front = 0;
    }

    pthread_mutex_unlock(&mutex);
    pthread_cond_signal(&que_not_full);

    return ret;
}

// int main()
// {
//     // for ( int i=0; i<QUE_MAX-1; ++i )
//     // {
//     //     Buffer b;
//     //     sprintf(&b.string, "%d", i);
//     //     que_enq(b);
//     // }
//     // while ( !que_is_empty() )
//     //     printf("%s ", que_deq().string);
//     // putchar('\n');
// }

```

---

## Banker's algorithm

Write a program to implement the banker’s algorithm for avoiding deadlock.  

The program will take input from the user to set up the appropriate resource tracking tables and output whether the system is in a is a safe state (along with the safe sequence of executions) or not.  

Input: Your program should prompt for and read the following inputs in order:  
* Number of processes P  
* Number of resources R  
* Max resource vector (length R vector represents the total number of each resource available to the system)  
* Current resource allocation table (P x R matrix represents how many of each resource is currently allocated to each process)  
* Maximum resource claim table (P x R matrix represents the maximum number of each resource that each process could ever require)  

Output: The very last line of output should state either “The system is in a safe state” or “The system is in an unsafe state”.  

Before printing that a state is safe, your program should output the sequence of resource grants that could be made to run each process to completion, thus demonstrating a safe state for the system.  

``` java
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

// ************************************** my implementation of Banker's algorithms with DFS **************************************


void print_1D_Arr(int n, int arr[])
{
	for (int i = 0; i < n; ++i)
	{
		printf("%d ", arr[i]);
	}
	printf("\n");

	return;
}

void print_2D_Arr(int m, int n, int arr[m][n])
{
	for (int i = 0; i < m; ++i)
	{
		for (int j = 0; j < n; ++j)
		{
			printf("%d ", arr[i][j]);
		}
		printf("\n");
	}
	return;
}

int sum_1D_Arr(int np, int arr[np]) {
	int sum = 0;
	for (int i = 0; i < np; ++i) {
		sum += arr[i];
	}
	return sum;
}

void copy_2D_arr1_to_arr2(int np, int nr, int a1[np][nr], int a2[np][nr]) {
	for (int i = 0; i < np; ++i) {
		for (int j = 0; j < nr; ++j) {
			a2[i][j] = a1[i][j];
		}
	}
}


void dfsHelper(int np, int nr, int avaiRsc[nr], int maxReq[np][nr], int curAllo[np][nr], int safeProc[np], int safe[1]) {
	if (safe[0]) {
		return;
	}
	if(sum_1D_Arr(np, safeProc) == np) {
		safe[0] = 1;
		printf(" ****************** Found the above solution! ****************** \n");
		return;
	}
	for (int i = 0; i < np; ++i) {
		if (safeProc[i] || safe[0] == 1) {
			continue;
		}
		int j = 0;
		int temp_avaiRsc[nr];
		int temp_curAllo[np][nr];
		int temp_shift[nr];
		copy_2D_arr1_to_arr2(np, nr, curAllo, temp_curAllo);
		// attempting to allocation.
		while (j < nr) {
			if (maxReq[i][j] - curAllo[i][j] > avaiRsc[j]) {
				break;
			}
			else {
				temp_shift[j] = maxReq[i][j] - curAllo[i][j];
				temp_avaiRsc[j] = avaiRsc[j] - temp_shift[j];
				temp_curAllo[i][j] = maxReq[i][j];
			}
			j++;
		}
		if (j == nr) {
			// update(np, nr, i, temp_avaiRsc, temp_curAllo, avaiRsc, curAllo, maxReq);
			printf("resources before allocation:\n");
			print_2D_Arr(np, nr, curAllo);

			printf("resources after allocation:\n");
			print_2D_Arr(np, nr, temp_curAllo);

			for (int k = 0; k < nr; ++k) {
				// take back resources
				avaiRsc[k] = temp_avaiRsc[k] + temp_curAllo[i][k];
				// now process finished::update allocation/request table
				maxReq[i][k] = 0;
				curAllo[i][k] = 0;
			}
			printf("Attempt to allocation resources to process # %d as follows successful!\n", i);
			print_1D_Arr(nr, temp_shift);
			printf("------------------------------------------------------------------------------\n");


			safeProc[i] = 1;
			dfsHelper(np, nr, avaiRsc, maxReq, curAllo, safeProc, safe);
			safeProc[i] = 0;
		}
	}
	return;
}

int main(int argc, char const *argv[])
{
	int rc = 0;

	int np = 0;
	rc = scanf("%d", &np);
	if (rc == 0 || rc == EOF) {
		printf("**** ERROR: missing number of processes.\n");
		exit(0);
	}
	printf("number of processes: %d\n", np);

	int nr = 0;
	rc = scanf("%d", &nr);
	if (rc == 0 || rc == EOF) {
		printf("**** ERROR: missing number of resources.\n");
		exit(0);
	}
	printf("number of resources: %d\n", nr);


	int maxRsc[nr];
	for (int i = 0; i < nr; i++)
	{
		int temp = 0;
		rc = scanf("%d", &temp);
		if (rc == 0 || rc == EOF) {
			printf("**** ERROR: missing number of resources.\n");
			exit(-1);
		}
		maxRsc[i] = temp;
	}
	printf("maximum resources:\n");
	print_1D_Arr(nr, maxRsc);


	int curAllo[np][nr];
	for (int i = 0; i < np; ++i) {
		for (int j = 0; j < nr; ++j) {
			int temp = 0;
			rc = scanf("%d", &temp);
			if (rc == 0 || rc == EOF) {
				printf("**** ERROR: missing number of resources.\n");
				exit(-1);
			}
			curAllo[i][j] = temp;
		}
	}
	printf("initial resource allocation:\n");
	print_2D_Arr(np, nr, curAllo);


	int avaiRsc[nr];
	for (int j = 0; j < nr; ++j) {
		avaiRsc[j] = maxRsc[j];
	}
	for (int i = 0; i < np; ++i) {
		for (int j = 0; j < nr; ++j) {
			avaiRsc[j] = avaiRsc[j] - curAllo[i][j];
		}
	}
	for (int j = 0; j < nr; ++j) {
		if (avaiRsc[j] < 0) {
			printf("**** ERROR: Current allocation of resources exceed maximum resources.\n");
			exit(-1);
		}
	}
	printf("available resources:\n");
	print_1D_Arr(nr, avaiRsc);


	int maxReq[np][nr];
	for (int i = 0; i < np; ++i) {
		for (int j = 0; j < nr; ++j) {
			int temp = 0;
			rc = scanf("%d", &temp);
			if (rc == 0 || rc == EOF) {
				printf("**** ERROR: missing number of resources.\n");
				exit(-1);
			}
			maxReq[i][j] = temp;
		}
	}
	printf("maximum resources request of each process:\n");
	print_2D_Arr(np, nr, maxReq);

	int safe[1];
	safe[0] = 0;
	int safeProc[np];
	for (int i = 0; i < np; ++i) {
		safeProc[i] = 0;
	}

	printf("****************** ready to check thread safety - resources share vs. deadlock issue. ******************\n");
	dfsHelper(np, nr, avaiRsc, maxReq, curAllo, safeProc, safe);

	if(safe[0] == 1) {
		printf("The system is in a safe state!\n");
	}
	else {
		printf("The system is in an unsafe state!\n");
	}

	return 0;
}
```
