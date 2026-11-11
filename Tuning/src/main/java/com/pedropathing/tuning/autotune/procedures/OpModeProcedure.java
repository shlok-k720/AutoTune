package com.pedropathing.tuning.autotune.procedures;

import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

public class OpModeProcedure extends Procedure {

    @Tuner(name = "OpMode Procedure")
    public static Procedure create() {
        return new OpModeProcedure();
    }

    public OpModeProcedure() {
        super("OpMode Procedure", "A Built-in Procedure for running OpModes");
    }

    @Override
    public void run() throws InterruptedException {
        
    }
    //TODO -> generate selector class/enum to input from all the opmodes
}
