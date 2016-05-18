GENERATOR :=$(wildcard *.generator)
STEP :=$(patsubst %.generator,%.step,$(GENERATOR))
HISTORY := $(wildcard *.history)
LP := $(patsubst %.history,%.lp,$(HISTORY))
GREEDY := $(patsubst %.history,%.greedy,$(HISTORY))
CPLEX :=$(patsubst %.lp,%.cplex,$(LP))
SOL := $(patsubst %.cplex,%.sol,$(CPLEX))
GREPPED := $(patsubst %.sol,%.grepped,$(SOL))
DATA := $(patsubst %.generator,%.data,$(GENERATOR))
all : $(STEP) $(ILP) $(GREEDY) $(CPLEX) $(SOL) $(DATA) $(GREPPED)

.SECONDARY:
%.step: %.generator
	java -jar Generator.jar $^ 10
	touch $@
%.lp : %.history
	/usr/bin/time -o $*.lp.time java -jar EHDraw.jar -nogui -input:$^ -exportilp -ilp_output:$@
	/usr/bin/time -o $*.load.time java -jar EHDraw.jar -nogui -input:$^
%.greedy : %.history
	/usr/bin/time -o $*.greedy.time java -jar EHDraw.jar -nogui -input:$^ -exportgreedy -greedy_output:$@
%.cplex : %.lp
	printf "set threads 1\nread "$^"\noptimize\nwrite "$*.sol"\nquit" > $@ 
%.sol : %.cplex
	/usr/bin/time -o $*.cplex.time cplex < $^ > $*.err
%.grepped : %.load.time %.greedy %.greedy.time %.sol %.lp.time %.cplex.time
	grep -Po '\K.*?(?=user)' $*.load.time > $@
	grep -Po ' \K.*?(?=system)' $*.load.time >> $@
	grep -Po 'size = \K.*?(?= ,)' $*.greedy >> $@
	grep -Po 'containing \K.*?(?= sets)' $*.greedy >> $@
	grep -Po 'sets, \K.*?(?=used)' $*.greedy >> $@
	grep -Po '\K.*?(?=user)' $*.greedy.time >> $@
	grep -Po ' \K.*?(?=system)' $*.greedy.time >> $@
	grep -Po 'objectiveValue="\K.*?(?=")' $*.sol >> $@
	grep -Po '\K.*?(?=user)' $*.lp.time >> $@
	grep -Po ' \K.*?(?=system)' $*.lp.time >> $@
	grep -Po '\K.*?(?=user)' $*.cplex.time >> $@
	grep -Po ' \K.*?(?=system)' $*.cplex.time >> $@
	-rm *.data
%.data : %.generator
	java -jar TestParser.jar $*

