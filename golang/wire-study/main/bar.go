package main

import "fmt"

type Bar struct {
	foo *Foo
}

func NewBar(foo *Foo) *Bar {
	return &Bar{
		foo: foo,
	}
}

func (p *Bar) Test() {
	fmt.Println("hello")
}
