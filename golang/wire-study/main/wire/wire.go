//+build wireinject

package wire

import (
	wire "github.com/google/wire"
)

var SuperSet = wire.NewSet(NewFoo, NewBar, NewBaz, NewBob)

func InitializeAllInstance() *Instance {
	wire.Build(SuperSet, Instance{})
	return &Instance{}
}
