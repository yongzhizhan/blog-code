// Code generated by Wire. DO NOT EDIT.

//go:generate wire
//+build !wireinject

package wire

import (
	"github.com/google/wire"
)

// Injectors from wire.go:

func InitializeAllInstance() *Instance {
	foo := NewFoo()
	bar := NewBar(foo)
	baz := NewBaz(foo, bar)
	instance := &Instance{
		Foo: foo,
		Bar: bar,
		Baz: baz,
	}
	return instance
}

// wire.go:

var SuperSet = wire.NewSet(NewFoo, NewBar, NewBaz, NewBob)