package fsm3.impl;

import akka.actor.AbstractFSM;
import fsm3.api.Data;
import fsm3.api.PowerOff;
import fsm3.api.PowerOn;
import fsm3.api.SimpleState;

/**
 * Created by rahul.ka on 04/05/16.
 */
public class SimpleFSMJ extends AbstractFSM<SimpleState, Data> {
    {
        startWith(OffState.Off, new SomeData());

        // transitions

        // from Off to On
        when(OffState.Off,
                matchEvent(PowerOn.class, SomeData.class,
                        (eventPowerOn, uninitialized) ->
                                goTo(OnState.On)
                                        .using(new SomeData())
                                        .replying(OnState.On)
                )
        );

        // from On to Off
        when(OnState.On,
                matchEvent(PowerOff.class, SomeData.class,
                        (eventPowerOff, uninitialized) ->
                                goTo(OffState.Off)
                                        .using(new SomeData())
                                        .replying(OffState.Off)
                )
        );

        onTermination(
                matchStop(Normal(),
                        (state, data) -> {
                            goTo(OffState.Off)
                                    .using(new SomeData());
                        }).
                        stop(Shutdown(),
                                (state, data) -> {
                                    goTo(OffState.Off)
                                            .using(new SomeData());
                                }).
                        stop(Failure.class,
                                (reason, state, data) -> {
                                    goTo(OffState.Off)
                                            .using(new SomeData());
                                })
        );
        initialize();
    }
}
